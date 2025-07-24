import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {QuestionService} from '../../../_services/question.service';
import {Question} from '../../../models/question';
import {CourseService} from '../../../_services/course.service';
import {PartService} from '../../../_services/part.service';
import {Part} from '../../../models/part';
import {Course} from '../../../models/course';
import {QuestionType} from '../../../models/question-type';
import {QuestionTypeService} from '../../../_services/question-type.service';
import {Location} from '@angular/common';

@Component({
  selector: 'app-question-detail',
  templateUrl: './question-detail.component.html',
  styleUrls: ['./question-detail.component.scss']
})
export class QuestionDetailComponent implements OnInit {

  questionId: number;
  questionInfo: Question;
  course: Course;
  imgDefaultUrl = '../../assets/images/avatar-default.png';
  questionTypeList: QuestionType[] = [];
  isEditMode = false;
  tempQuestion: Question; // Biến lưu tạm khi sửa

  constructor(
    private route: ActivatedRoute,
    private questionService: QuestionService,
    private courseService: CourseService,
    private questionTypeService: QuestionTypeService,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.getQuestion();
  }

  getQuestion() {
    this.questionId = Number(this.route.snapshot.paramMap.get('questionId'));

    this.questionService.getQuestionById(this.questionId).subscribe(res => {
      this.questionInfo = res;
      this.courseService.getCourseByPartId(this.questionInfo.part.id).subscribe(data => {
        this.course = data;
      });
    });
    this.isEditMode = false; // Reset lại chế độ sửa khi load lại
  }

  goBack() {
    this.location.back();
  }

  enableEdit() {
    this.isEditMode = true;
    this.tempQuestion = JSON.parse(JSON.stringify(this.questionInfo));

    // Nếu là TF mà thiếu 1 trong 2 đáp án, bổ sung lại
    if (this.tempQuestion.questionType.typeCode === 'TF') {
      if (!this.tempQuestion.choices || this.tempQuestion.choices.length < 2) {
        this.tempQuestion.choices = [
          { id: null, choiceText: 'True', isCorrected: 1 },
          { id: null, choiceText: 'False', isCorrected: 0 }
        ];
      } else {
        // Đảm bảo True/False đúng vị trí
        let trueChoice = this.tempQuestion.choices.find(c => c.choiceText === 'True');
        let falseChoice = this.tempQuestion.choices.find(c => c.choiceText === 'False');
        if (!trueChoice) this.tempQuestion.choices.push({ id: null, choiceText: 'True', isCorrected: 0 });
        if (!falseChoice) this.tempQuestion.choices.push({ id: null, choiceText: 'False', isCorrected: 0 });
        // Sắp xếp lại True trước, False sau
        this.tempQuestion.choices.sort((a, b) => a.choiceText === 'True' ? -1 : 1);
      }
    }
  }

  cancelEdit() {
    this.isEditMode = false;
  }

  addChoice() {
    this.tempQuestion.choices.push({
      id: null, // Nếu thêm mới
      choiceText: '',
      isCorrected: 0
    });
  }

  removeChoice(idx: number) {
    if (this.tempQuestion.choices.length > 2) {
      this.tempQuestion.choices.splice(idx, 1);
    }
  }

  setCorrectChoice(idx: number, multi: boolean = false) {
    if (this.tempQuestion.questionType.typeCode === 'MC') {
      // Chỉ 1 đáp án đúng
      this.tempQuestion.choices.forEach((c, i) => c.isCorrected = (i === idx) ? 1 : 0);
    } else if (this.tempQuestion.questionType.typeCode === 'MS') {
      // Chọn nhiều đáp án đúng
      this.tempQuestion.choices[idx].isCorrected = this.tempQuestion.choices[idx].isCorrected ? 0 : 1;
    } else if (this.tempQuestion.questionType.typeCode === 'TF') {
      // True/False
      this.tempQuestion.choices.forEach((c, i) => c.isCorrected = (i === idx) ? 1 : 0);
    }
  }

  validateQuestion(): boolean {
    // Không để trống nội dung câu hỏi, MC/TF chỉ có 1 đáp án đúng, MS >= 1 đáp án đúng, point phải có giá trị >= 0
    if (
      !this.tempQuestion.questionText ||
      !this.tempQuestion.choices || 
      this.tempQuestion.choices.length < 2 ||
      this.tempQuestion.point == null ||
      this.tempQuestion.point < 0
    ) return false;
    if (this.tempQuestion.questionType.typeCode === 'MC' || this.tempQuestion.questionType.typeCode === 'TF') {
      return this.tempQuestion.choices.filter(c => c.isCorrected == 1).length === 1;
    }
    if (this.tempQuestion.questionType.typeCode === 'MS') {
      return this.tempQuestion.choices.filter(c => c.isCorrected == 1).length >= 1;
    }
    return true;
  }

  saveEdit() {
    if (!this.validateQuestion()) {
      alert('Vui lòng kiểm tra lại dữ liệu trước khi lưu!');
      return;
    }
    if (confirm('Bạn chắc chắn muốn lưu các thay đổi cho câu hỏi này?')) {
      // Nếu là TF, chỉ giữ lại đáp án đúng
      if (this.tempQuestion.questionType.typeCode === 'TF') {
        const correctChoice = this.tempQuestion.choices.find(c => c.isCorrected == 1);
        if (correctChoice) {
          this.tempQuestion.choices = [{
            id: correctChoice.id || null,
            choiceText: correctChoice.choiceText,
            isCorrected: 1
          }];
        }
      }
      // debug:
      console.log('Dữ liệu gửi lên:', this.tempQuestion);
      this.questionService.updateQuestion(this.questionInfo.id, this.tempQuestion).subscribe(
        res => {
          alert('Cập nhật thành công!');
          this.getQuestion();
        },
        err => {
          alert('Cập nhật thất bại!');
        }
      );
    }
  }

  get isTrueCorrectAnswer(): boolean {
    if (!this.questionInfo?.choices) return false;
    // Nếu chỉ còn 1 choice thì check giá trị
    if (this.questionInfo.choices.length === 1) {
      return this.questionInfo.choices[0].choiceText === 'True' && this.questionInfo.choices[0].isCorrected == 1;
    }
    // Có cả 2 đáp án thì tìm đáp án đúng
    return this.questionInfo.choices.find(c => c.choiceText === 'True' && c.isCorrected == 1) !== undefined;
  }
  
  get isFalseCorrectAnswer(): boolean {
    if (!this.questionInfo?.choices) return false;
    if (this.questionInfo.choices.length === 1) {
      return this.questionInfo.choices[0].choiceText === 'False' && this.questionInfo.choices[0].isCorrected == 1;
    }
    return this.questionInfo.choices.find(c => c.choiceText === 'False' && c.isCorrected == 1) !== undefined;
  }
  
}
