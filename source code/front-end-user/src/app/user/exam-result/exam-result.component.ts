import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ExamService} from '../../_services/exam.service';
import {ExamResult} from '../../models/exam-result';

@Component({
  selector: 'app-exam-result',
  templateUrl: './exam-result.component.html',
  styleUrls: ['./exam-result.component.scss']
})
export class ExamResultComponent implements OnInit {

  examId: number;
  result: ExamResult;
  maxPoint: number = 100; // ✅ Thêm biến này

  constructor(
    private activatedRoute: ActivatedRoute,
    private examService: ExamService) {
  }

  ngOnInit(): void {
    this.examId = Number(this.activatedRoute.snapshot.paramMap.get('examId'));
    this.getResult(this.examId);
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
  
  getResult(examId: number) {
    this.examService.getExamUserResult(examId).subscribe(value => {
      this.result = value;
      // ✅ Xử lý fallback maxPoint nếu thiếu
      if (this.result && this.result.exam && this.result.exam.maxPoint !== undefined && this.result.exam.maxPoint !== null) {
        this.maxPoint = this.result.exam.maxPoint;
      } else {
        this.maxPoint = 100;
      }
    });
  }
}
