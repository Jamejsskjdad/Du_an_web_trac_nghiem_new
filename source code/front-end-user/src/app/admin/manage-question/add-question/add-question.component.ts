import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { PageResult } from '../../../models/page-result';
import { Question } from '../../../models/question';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CourseService } from '../../../_services/course.service';
import { Course } from '../../../models/course';
import { Part } from '../../../models/part';
import { PartService } from '../../../_services/part.service';
import { QuestionTypeService } from '../../../_services/question-type.service';
import { QuestionType } from '../../../models/question-type';
import { QTYPE } from '../../../models/question-type.enum';
import { Choice } from '../../../models/choice';
import * as BalloonEditor from '@ckeditor/ckeditor5-build-balloon';
import { QuestionService } from '../../../_services/question.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-add-question',
  templateUrl: './add-question.component.html',
  styleUrls: ['./add-question.component.scss']
})
export class AddQuestionComponent implements OnInit {
  @Output() questionsAddOutput = new EventEmitter<PageResult<Question>>();

  rfAdd: FormGroup;
  editorValue = '';
  showModal = false;
  courseList: Course[] = [];
  partList: Part[] = [];
  questionTypeList: QuestionType[] = [];
  selectedCourseId = -1;
  selectedPartId = -1;
  answerChoicesTF: Choice[] = [];
  currentQuestionType = QTYPE.TF;
  multipleChoice: Choice[] = [];
  questionText = '';
  shortAnswer = '';
  Editor = BalloonEditor;
  editorConfig = {
    placeholder: 'Nhập nội dung!',
  };
  multipleSelect: Choice[] = [];

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private partService: PartService,
    private questionTypeService: QuestionTypeService,
    private questionService: QuestionService,
    private toast: ToastrService) {
  }

  get course() { return this.rfAdd.get('course'); }
  get questionType() { return this.rfAdd.get('questionType'); }
  get part() { return this.rfAdd.get('part'); }
  get choices() { return this.rfAdd.get('choices'); }
  get point() { return this.rfAdd.get('point'); }

  ngOnInit(): void {
    this.rfAdd = this.fb.group({
      course: ['-1'],
      questionType: [this.currentQuestionType],
      part: [''],
      choices: ['True'],
      point: [null, [Validators.required, Validators.min(0)]], // Điểm là trường bắt buộc
    });
  }

  onSubmit() {
    const pointValue = this.point.value;
    if (pointValue === null || isNaN(pointValue) || pointValue < 0) {
      this.toast.error('Điểm không hợp lệ', 'Lỗi');
      return;
    }
    if (!this.questionText || this.questionText.trim() === '') {
      this.toast.error('Bạn phải nhập nội dung câu hỏi!', 'Lỗi');
      return;
    }

    let questionPayload: any = {
      questionText: this.questionText,
      point: pointValue,
      choices: []
    };

    switch (this.questionType.value) {
      case QTYPE.TF: {
        // Đúng/Sai: phải truyền đủ 2 đáp án
        questionPayload.choices = [
          new Choice('Đúng', this.choices.value === 'True' ? 1 : 0),
          new Choice('Sai', this.choices.value === 'False' ? 1 : 0),
        ];
        break;
      }
      case QTYPE.MC: {
        // Một đáp án đúng
        if (this.multipleChoice.length < 2) {
          this.toast.error('Phải có ít nhất 2 đáp án!', 'Lỗi');
          return;
        }
        questionPayload.choices = this.multipleChoice.map(c => ({ choiceText: c.choiceText, isCorrected: c.isCorrected }));
        break;
      }
      case QTYPE.MS: {
        // Nhiều đáp án đúng
        if (this.multipleSelect.length < 2) {
          this.toast.error('Phải có ít nhất 2 đáp án!', 'Lỗi');
          return;
        }
        questionPayload.choices = this.multipleSelect.map(c => ({ choiceText: c.choiceText, isCorrected: c.isCorrected }));
        break;
      }
      case QTYPE.SA: { // <<<<< THÊM CASE NÀY
        if (!this.shortAnswer || this.shortAnswer.trim() === '') {
          this.toast.error('Bạn phải nhập đáp án cho câu trả lời ngắn!', 'Lỗi');
          return;
        }
        questionPayload.choices = [
          new Choice(this.shortAnswer, 1) // Đáp án đúng duy nhất
        ];
        break;
      }
      default:
        this.toast.error('Loại câu hỏi không hợp lệ!', 'Lỗi');
        return;
    }
    

    // Gọi API
    this.questionService.createQuestion(
      questionPayload, 
      this.questionType.value, 
      this.part.value
    ).subscribe(
      res => {
        this.toast.success('Tạo câu hỏi thành công');
        this.resetFormAfterSubmit();
      },
      error => {
        this.toast.error('Kiểm tra lại thông tin cần tạo', 'Lỗi');
      }
    );
  }

  toggleModal() {
    this.showModal = !this.showModal;
    this.refreshModal();
    this.courseService.getCourseList().subscribe(res => {
      this.courseList = res;
    });
    this.questionTypeService.getQuestionTypeList().subscribe(res => {
      this.questionTypeList = res;
    });
    this.initChoice();
  }

  initChoice() {
    this.multipleChoice.length = 0;
    this.multipleSelect.length = 0;
    this.multipleChoice.push(
      new Choice('<p>Đáp án 1</p>', 1),
      new Choice('<p>Đáp án 2</p>', 0)
    );
    this.multipleSelect.push(
      new Choice('<p>Đáp án 1</p>', 1),
      new Choice('<p>Đáp án 2</p>', 0)
    );
  }

  refreshModal() {
    this.course.setValue(-1);
    this.questionType.setValue(QTYPE.TF);
    this.part.setValue(-1);
    this.rfAdd.get('point').setValue(null);
    this.multipleChoice.length = 0;
    this.multipleSelect.length = 0;
    this.currentQuestionType = QTYPE.TF;
    this.questionText = '';
  }

  closeModal() {
    this.showModal = false;
  }

  changeCourse(event) {
    this.selectedCourseId = event.target.value;
    this.partService.getPartsByCourse(this.selectedCourseId).subscribe(res => {
      this.partList = res;
    });
  }

  changeQuestionType(typeCode: string) {
    this.currentQuestionType = QTYPE[typeCode];
  }

  changeChoiceTF(value: string) {
    this.rfAdd.get('choices').setValue(value);
  }

  addMCAnswer() {
    this.multipleChoice.push(new Choice('', 0));
  }

  changeChoiceMC(i: number) {
    this.multipleChoice.forEach((item, idx) => item.isCorrected = (i === idx ? 1 : 0));
  }

  removeMCChoice(i: number) {
    if (this.multipleChoice.length <= 2) return;
    this.multipleChoice.splice(i, 1);
  }

  changeChoiceMS(i: number) {
    this.multipleSelect[i].isCorrected = this.multipleSelect[i].isCorrected ? 0 : 1;
  }

  removeMSChoice(index: number) {
    if (this.multipleSelect.length <= 2) return;
    this.multipleSelect.splice(index, 1);
  }

  addMSAnswer() {
    this.multipleSelect.push(new Choice('', 0));
  }

  resetFormAfterSubmit() {
    this.questionText = '';
    this.multipleSelect.length = 0;
    this.multipleChoice.length = 0;
    this.selectedPartId = -1;
    this.rfAdd.get('point').setValue(null);
    this.initChoice();
  }
}
