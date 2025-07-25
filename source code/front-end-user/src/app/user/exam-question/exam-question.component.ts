import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Exam} from '../../models/exam';
import {Question} from '../../models/question';
import {ExamService} from '../../_services/exam.service';
import {AnswerSheet} from '../../models/answer-sheet';
import {interval, Subscription, timer} from 'rxjs';
import { SidebarService } from '../../_services/sidebar.service';

@Component({
  selector: 'app-exam-question',
  templateUrl: './exam-question.component.html',
  styleUrls: ['./exam-question.component.scss']
})
export class ExamQuestionComponent implements OnInit, OnDestroy {
  examId: number;
  exam: Exam;
  questions: Question[] = [];
  toggleModal = false;
  answerSheets: AnswerSheet[] = [];
  choicesSelected: any[] = [];
  countDown: Subscription;
  counter: number;
  tick = 1000;
  private subscription: any;
  private tabSwitchCount = 0;

  private examFinished = false; // ✅ Trạng thái kết thúc bài thi
  private visibilityHandler = this.handleTabSwitch.bind(this); // ✅ Gỡ đúng listener

  constructor(
    private activatedRoute: ActivatedRoute,
    private examService: ExamService,
    private router: Router,
    private sidebarService: SidebarService // ✅ Thêm dòng này
  ) { }
  
  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.countDown?.unsubscribe();
    this.removeSecurityListeners();
  }

  ngOnInit(): void {
    this.examId = Number(this.activatedRoute.snapshot.paramMap.get('examId'));

    // ✅ Kiểm tra trạng thái bài thi
    this.examService.getExamUserById(this.examId).subscribe(examUser => {
      if (examUser.isFinished) {
        this.router.navigate(['/user/exams', this.examId, 'result']);
      } else {
        this.getQuestion();
      }
    });
  }
  startTimer() {
    this.countDown = timer(0, this.tick).subscribe(() => {
      if (this.counter > 0) {
        --this.counter;
      } else {
        this.counter = 0;
        this.submit();
      }
    });
  }

  getQuestion() {
    this.examId = Number(this.activatedRoute.snapshot.paramMap.get('examId'));
    this.examService.getExamQuestion(this.examId).subscribe(res => {
      this.questions = res.questions;
      this.exam = res.exam;
      this.counter = res.remainingTime;
      this.startTimer();
      this.subscription = interval(10000).subscribe(x => {
        this.saveToServer(false);
      });
  
      // === Thêm dòng sau để ẩn sidebar khi lockScreen bật ===
      if (this.exam.lockScreen) {
        this.sidebarService.setSidebarCollapsed(true); // Ẩn sidebar
        this.requestFullscreen();
        this.preventCopyPaste();
        this.detectTabSwitching();
      }
    });
  }
  

  requestFullscreen() {
    const docElm = document.documentElement as any;
    if (docElm.requestFullscreen) {
      docElm.requestFullscreen();
    } else if (docElm.mozRequestFullScreen) {
      docElm.mozRequestFullScreen();
    } else if (docElm.webkitRequestFullscreen) {
      docElm.webkitRequestFullscreen();
    } else if (docElm.msRequestFullscreen) {
      docElm.msRequestFullscreen();
    }
  }

  preventCopyPaste() {
    document.addEventListener('copy', this.blockEvent);
    document.addEventListener('cut', this.blockEvent);
    document.addEventListener('paste', this.blockEvent);
  }

  detectTabSwitching() {
    document.addEventListener('visibilitychange', this.visibilityHandler);
  }

  handleTabSwitch() {
    if (this.examFinished) return; // ✅ Nếu đã nộp bài thì bỏ qua

    if (document.hidden) {
      this.tabSwitchCount++;
      alert('⚠️ Bạn đã chuyển tab! Nếu tiếp tục, bài thi sẽ bị nộp.');
      if (this.tabSwitchCount >= 2) {
        alert('🚨 Bạn đã chuyển tab nhiều lần. Bài thi sẽ bị nộp.');
        this.submit();
      }
    }
  }

  blockEvent(e: ClipboardEvent) {
    e.preventDefault();
    alert('🚫 Không được phép sao chép, cắt hoặc dán trong khi làm bài.');
  }

  removeSecurityListeners() {
    document.removeEventListener('copy', this.blockEvent);
    document.removeEventListener('cut', this.blockEvent);
    document.removeEventListener('paste', this.blockEvent);
    document.removeEventListener('visibilitychange', this.visibilityHandler);
  }

  selectAnswerTF(value: any, id: number) {
    const question = this.questions.find(item => item.id === id);
    if (question) {
      question.choices[0].isCorrected = 1;
      question.choices[0].choiceText = value.target.value; // đảm bảo text đúng
    }
  }
  
  selectedAnswerMC(value: any, questionId: number, choiceId: number) {
    const question = this.questions.find(x => x.id === questionId);
    question.choices.map(x => x.id === choiceId ? (x.isCorrected = 1) : (x.isCorrected = 0));
  }

  selectedAnswerMS($event: Event, questionId: number, choiceId: number) {
    const question = this.questions.find(x => x.id === questionId);
    const choice = question.choices.find(y => y.id === choiceId);
    choice.isCorrected = choice.isCorrected ? 0 : 1;
  }

  showModal() {
    this.choicesSelected.length = 0;
    this.toggleModal = true;
    this.questions.forEach(value => {
      let selected = false;
      if (value.questionType.typeCode === 'SA') {
        // Nếu là câu trả lời ngắn thì kiểm tra userAnswer có nội dung không
        selected = value.userAnswer && value.userAnswer.trim() !== '';
      } else {
        // Câu khác: đã chọn đáp án
        selected = value.choices.some(x => x.isCorrected === 1);
      }
      this.choicesSelected.push({ selected });
    });
  }
  

  closeModal() {
    this.toggleModal = false;
  }

  submit() {
    const answerSheets: AnswerSheet[] = [];
    this.questions.forEach(value => {
      // Nếu là câu hỏi Short Answer (SA), đóng gói đáp án người dùng vào choices[0].choiceText
      if (value.questionType.typeCode === 'SA') {
        value.choices = [
          {
            ...value.choices[0], // giữ id (nếu có), trường hợp backend cần id để so sánh
            choiceText: value.userAnswer || '', // userAnswer là property bạn phải binding từ ô input ở HTML cho từng câu SA
            isCorrected: 1 // đánh dấu là đã điền đáp án
          }
        ];
      }
      answerSheets.push(new AnswerSheet(value.id, value.choices, value.point));
    });
  
    this.examFinished = true;
    this.countDown?.unsubscribe();
    this.subscription?.unsubscribe();
    this.removeSecurityListeners();
    this.exitFullscreen();
    this.sidebarService.setSidebarCollapsed(false);
    this.examService.submitExamUser(this.examId, true, this.counter, answerSheets).subscribe(res => {
      this.router.navigate(['../result'], { relativeTo: this.activatedRoute });
    }, error => {
      console.log('error');
    });
  }
  
  exitFullscreen() {
    const doc: any = document;
    if (doc.exitFullscreen) {
      doc.exitFullscreen();
    } else if (doc.mozCancelFullScreen) {
      doc.mozCancelFullScreen();
    } else if (doc.webkitExitFullscreen) {
      doc.webkitExitFullscreen();
    } else if (doc.msExitFullscreen) {
      doc.msExitFullscreen();
    }
  }
  
  saveToServer(isFinished: boolean) {
    const answerSheets: AnswerSheet[] = [];
    this.questions.forEach(value => {
      answerSheets.push(new AnswerSheet(value.id, value.choices, value.point));
    });
    this.examService.submitExamUser(this.examId, isFinished, this.counter, answerSheets).subscribe(res => {
      console.log('ok');
    }, error => {
      console.log('error');
    });

    console.log(this.questions);
  }
}
