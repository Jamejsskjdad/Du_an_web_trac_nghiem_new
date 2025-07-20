import {Component, OnInit, ViewChild} from '@angular/core';
import {ExamService} from '../../../_services/exam.service';
import {ExamResult} from '../../../models/exam-result';
import {ActivatedRoute, Router} from '@angular/router';
import _ from 'lodash';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexPlotOptions,
  ApexStroke,
  ApexTitleSubtitle,
  ApexXAxis,
  ApexYAxis,
  ChartComponent
} from 'ng-apexcharts';
import {ColorsService} from '../../../_services/colors.service';
import {ExamQuestionReport} from '../../../models/exam-question-report';
import {Location} from '@angular/common';

export interface ChartOptions {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xAxis: ApexXAxis;
  stroke: ApexStroke;
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  yAxis: ApexYAxis;
  colors: string[];
  subtitle: ApexTitleSubtitle;
}

@Component({
  selector: 'app-user-test',
  templateUrl: './user-test.component.html',
  styleUrls: ['./user-test.component.scss']
})
export class UserTestComponent implements OnInit {
  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: Partial<ChartOptions>;
  examResults: ExamResult[] = [];
  examId: number;
  sortBy = false;
  dataSort: ExamResult[] = [];
  questionsReport: ExamQuestionReport[] = [];
  skeleton = true;

  constructor(
    private examService: ExamService,
    private route: ActivatedRoute,
    private colorsService: ColorsService,
    private router: Router,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.examId = Number(this.route.snapshot.paramMap.get('id'));
    this.getExamList();
    this.getQuestionReport();
  }

  getPercent(x: number, size: number) {
    if (!size || size === 0) return 0;
    return Math.round((x / size) * 100);
  }
  

  getQuestionReport() {
    this.examService.getExamQuestionReport(this.examId).subscribe(data => {
      this.questionsReport = data;
      this.skeleton = false;
    }, error => {
      this.skeleton = false;
    });
  }
  getFinishedUserCount(): number {
    // Tùy backend quy ước examStatus: -1 = Hoàn thành (thường vậy)
    return this.examResults.filter(e =>
      e.examStatus === -1 &&
      typeof e.totalPoint === 'number' &&
      !isNaN(e.totalPoint)
    ).length;
  }
  
  getExamList() {
    this.examService.getExamResultListByExamId(this.examId).subscribe(data => {
      this.examResults = data;
  
      // Lọc user hợp lệ và điểm là số hợp lệ
      const validResults = this.examResults.filter(
        exam => exam.user && exam.user.profile && typeof exam.totalPoint === 'number' && !isNaN(exam.totalPoint)
      );
      // Sắp xếp giảm dần theo điểm, lấy top 10
      this.dataSort = _.orderBy(validResults, 'totalPoint', 'desc').slice(0, 10);
  
      // Màu cố định (tuỳ ý bạn, thêm màu nếu >10 user)
      const fixedColors = [
        '#4CAF50', // xanh lá
        '#2196F3', // xanh dương
        '#FFC107', // vàng
        '#E91E63', // hồng
        '#9C27B0', // tím
        '#FF5722', // cam
        '#3F51B5', // xanh tím
        '#009688', // teal
        '#795548', // nâu
        '#607D8B', // xám xanh
      ];
  
      this.chartOptions = {
        series: [{
          name: 'Result',
          data: this.dataSort.map(value => value.totalPoint ?? NaN)
        }],
        chart: {
          type: 'bar',
          height: 380
        },
        plotOptions: {
          bar: {
            barHeight: '100%',
            distributed: true,
            horizontal: true,
            dataLabels: {
              position: 'bottom'
            }
          }
        },
        colors: fixedColors.slice(0, this.dataSort.length), // Dùng đúng số màu cho số user
        dataLabels: {
          enabled: true,
          textAnchor: 'start',
          style: {
            colors: ['#fff']
          },
          formatter(val, opt) {
            // Nếu label bị undefined sẽ trả về chuỗi rỗng
            const label = opt.w.globals.labels[opt.dataPointIndex] || '';
            return label + ': ' + val + ' point';
          },
          offsetX: 0,
          dropShadow: {
            enabled: true
          }
        },
        stroke: {
          width: 1,
          colors: ['#fff']
        },
        xAxis: {
          categories: this.dataSort.map(val => 
            val.user?.profile
              ? `${val.user.profile.lastName} ${val.user.profile.firstName}`
              : '[No name]'
          )
        },
        yAxis: {
          labels: { show: false }
        },
        subtitle: {
          text: 'Top 10 điểm số cao nhất',
          align: 'center'
        }
      };
  
      // Log các giá trị để kiểm tra trực quan trên console
      console.log('Data hiển thị trên biểu đồ:', this.dataSort);
      console.log('chartOptions:', this.chartOptions);
    });
  }
  
  

  trackById(index: number, item: ExamResult): any {
    console.log('trackById', item?.user?.username ?? index);
    return item?.user?.username ?? index;
  }
  
  sortPoint() {
    this.sortBy = !this.sortBy;
    this.dataSort = _.orderBy(this.examResults, 'totalPoint', this.sortBy ? 'desc' : 'asc');
  }
  

  goUserExamDetail(username: string, userProfile: string) {
    this.router.navigate([`admin/tests/${this.examId}/users`, username]);
  }

  goDeTail(id: number) {
    this.router.navigate(['admin/question-bank/question', id]);
  }

  goBack() {
    this.location.back();
  }
}
