import { Component, OnInit } from '@angular/core';
import { ExamService } from '../../_services/exam.service';
import { Exam } from '../../models/exam';
import { PaginationDetail } from '../../models/pagination/pagination-detail';
import { delay } from 'rxjs/operators';
import * as moment from 'moment';

@Component({
  selector: 'app-manage-test',
  templateUrl: './manage-test.component.html',
  styleUrls: ['./manage-test.component.scss']
})
export class ManageTestComponent implements OnInit {
  examList: Exam[] = [];
  paginationDetail: PaginationDetail;
  skeleton = true;
  now = moment(new Date()).format('YYYY-MM-DD HH:mm:ss');

  pageOptions: any = [
    { display: 20, num: 20 },
    { display: 50, num: 50 },
    { display: 100, num: 100 },
    { display: 'Tất cả', num: '' },
  ];
  pageCountShowing = 20;
  selectMode = false; // Trạng thái chọn/xóa
  selectedExamIds: number[] = []; // Danh sách id các bài thi được chọn
  allChecked = false; // Trạng thái checkbox "chọn tất cả"  
  constructor(private examService: ExamService) {
  }

  ngOnInit(): void {
    this.fetchExamList();
  }

  fetchExamList() {
    this.examService.getAllExams(0, 20).subscribe(res => {
      this.examList = res.data;
      this.paginationDetail = res.paginationDetails;
      this.skeleton = false;
    });
  }

  trackById(item, index) {
    return item.id === index;
  }

  changePageShow(value: any) {
    this.pageCountShowing = value;
    if (!value) {
      this.skeleton = true;
      this.examService.getAllExams(0, this.paginationDetail.totalCount).subscribe(res => {
        this.examList = res.data;
        this.paginationDetail = res.paginationDetails;
        this.skeleton = false;
      });
    } else {
      this.skeleton = true;
      this.examService.getAllExams(0, value).subscribe(res => {
        this.examList = res.data;
        this.paginationDetail = res.paginationDetails;
        this.skeleton = false;
      });
    }
  }

  getStatusExam(exam: Exam) {
    const beginDate = moment(exam.beginExam).format('YYYY-MM-DD HH:mm:ss');
    const finishDate = moment(exam.finishExam).format('YYYY-MM-DD HH:mm:ss');

    if (moment(beginDate).isAfter(this.now)) {
      return 0;
    } else if (moment(finishDate).isBefore(this.now)) {
      return -1;
    }
    return 1;
  }

  goPreviousPage() {
    const isFirstPage: boolean = this.paginationDetail.isFirstPage;
    if (!isFirstPage) {
      this.examService.getAllExams(this.paginationDetail.previousPage.pageNumber, this.pageCountShowing)
        .subscribe(res => {
          this.examList = res.data;
          this.paginationDetail = res.paginationDetails;
        });
    }

  }

  goNextPage() {
    const isLastPage = !this.paginationDetail.nextPage.available;
    if (!isLastPage) {
      this.examService.getAllExams(this.paginationDetail.nextPage.pageNumber, this.pageCountShowing
      ).subscribe(res => {
        this.examList = res.data;
        this.paginationDetail = res.paginationDetails;
      });
    }
  }
  enterSelectMode() {
    this.selectMode = true;
    this.selectedExamIds = [];
    this.allChecked = false;
  }
  cancelSelectMode() {
    this.selectMode = false;
    this.selectedExamIds = [];
    this.allChecked = false;
  }
  toggleExamSelection(examId: number, checked: boolean) {
    if (checked) {
      if (!this.selectedExamIds.includes(examId)) {
        this.selectedExamIds.push(examId);
      }
    } else {
      this.selectedExamIds = this.selectedExamIds.filter(id => id !== examId);
    }
    // Nếu bỏ chọn bất kỳ checkbox, bỏ chọn "chọn tất cả"
    if (!checked) {
      this.allChecked = false;
    } else if (this.selectedExamIds.length === this.examList.length) {
      this.allChecked = true;
    }
  }

  toggleSelectAll(checked: boolean) {
    this.allChecked = checked;
    if (checked) {
      this.selectedExamIds = this.examList.map(e => e.id);
    } else {
      this.selectedExamIds = [];
    }
  }
  onDeleteSelectedExams() {
    if (confirm(`Bạn chắc chắn muốn xóa ${this.selectedExamIds.length} bài thi đã chọn?`)) {
      this.examService.deleteManyExams(this.selectedExamIds).subscribe(
        res => {
          this.fetchExamList();
          this.cancelSelectMode();
        },
        err => {
          alert('Xóa thất bại!');
        }
      );
    }
  }

}
