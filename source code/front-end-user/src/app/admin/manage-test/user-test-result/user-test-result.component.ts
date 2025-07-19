import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ExamService} from '../../../_services/exam.service';
import {ExamResult} from '../../../models/exam-result';
import {Location} from '@angular/common';
import {UserService} from '../../../_services/user.service';
import {UserAccount} from '../../../models/user-account';

@Component({
  selector: 'app-user-test-result',
  templateUrl: './user-test-result.component.html',
  styleUrls: ['./user-test-result.component.scss']
})
export class UserTestResultComponent implements OnInit {

  username: string;
  examId: number;
  result: ExamResult;
  user: UserAccount;
  maxPoint: number = 100; // ✅ Thêm biến này, mặc định 100

  constructor(private route: ActivatedRoute,
              private examService: ExamService,
              private userService: UserService,
              private location: Location) {
  }

  ngOnInit(): void {
    this.getResult();
  }

  getResult() {
    this.username = this.route.snapshot.paramMap.get('username');
    this.examId = Number(this.route.snapshot.paramMap.get('id'));
    this.examService.getResultExamByUser(this.examId, this.username).subscribe(res => {
      this.result = res;
      // ✅ Gán maxPoint từ backend, fallback 100 nếu không có
      if (res && res.exam && res.exam.maxPoint !== undefined && res.exam.maxPoint !== null) {
        this.maxPoint = res.exam.maxPoint;
      } else {
        this.maxPoint = 100;
      }
      // debug
      console.log('Max Point:', this.maxPoint);
      // debug questions
      this.result.choiceList.forEach((ques, index) => {
        console.log(`--- Question ${index + 1} ---`);
        console.log('Question Text:', ques.question.questionText);
        console.log('Choices:', ques.choices.map(c => ({
          text: c.choice.choiceText,
          isCorrected: c.choice.isCorrected,
          isRealCorrect: c.isRealCorrect
        })));
      }); 
    });
    this.userService.getUserInfo(this.username).subscribe(res => {
      this.user = res.data;
    });
  }
  getFailMax(): number {
    return Math.floor(this.maxPoint * 0.49);
  }
  
  getAverageMin(): number {
    return Math.floor(this.maxPoint * 0.5);
  }
  getAverageMax(): number {
    return Math.floor(this.maxPoint * 0.79);
  }
  getPassMin(): number {
    return Math.floor(this.maxPoint * 0.8);
  }
  
  goBack() {
    this.location.back();
  }
}
