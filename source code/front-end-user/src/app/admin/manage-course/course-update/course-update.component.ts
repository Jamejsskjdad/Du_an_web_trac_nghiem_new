import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Course} from '../../../models/course';
import {PageResult} from '../../../models/page-result';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CourseService} from '../../../_services/course.service';
import {ToastrService} from 'ngx-toastr';
import {Intake} from '../../../models/intake';
import {IntakeService} from '../../../_services/intake.service';
import {switchMap} from 'rxjs/operators';

@Component({
  selector: 'app-course-update',
  templateUrl: './course-update.component.html',
  styleUrls: ['./course-update.component.scss']
})
export class CourseUpdateComponent implements OnInit {

  @Input() courseInfo: Course;
  @Output() courseOutput = new EventEmitter<PageResult<Course>>();
  showModalUpdate = false;
  rfUpdateCourse: FormGroup;
  showLoading = false;
  pageResult: PageResult<Course>;
  course: Course;
  toggle = false;
  intakeList: Intake[] = [];

  constructor(private fb: FormBuilder,
              private courseService: CourseService,
              private intakeService: IntakeService,
              private toast: ToastrService) {
  }

  get courseCode() {
    return this.rfUpdateCourse.get('courseCode');
  }

  get courseName() {
    return this.rfUpdateCourse.get('courseName');
  }

  ngOnInit(): void {
    this.course = Object.assign({}, this.courseInfo);
    this.intakeService.getIntakeList().subscribe(data => {
      this.intakeList = data;
      const intakeId = this.course?.intakes?.[0]?.id || null;
      this.initForm(intakeId);
    });
  }

  initForm(intakeId: number | null = null) {
    this.rfUpdateCourse = this.fb.group({
      courseCode: [this.course.courseCode, {
        validators: [Validators.required],
        asyncValidators: [this.courseService.validateCourseCode(this.course.id)],
        updateOn: 'blur'
      }],
      courseName: [this.course.name, Validators.required],
      intakeId: [intakeId, Validators.required]
    });
  }

  toggleModalUpdate() {
    this.course = Object.assign({}, this.courseInfo);
    const intakeId = this.course?.intakes?.[0]?.id || null;
    this.initForm(intakeId);
    this.showModalUpdate = !this.showModalUpdate;
  }

  onSubmit() {
    this.showLoading = true;
    const course: Course = new Course(this.courseCode.value, this.courseName.value);
    const intakeId = this.rfUpdateCourse.get('intakeId')?.value;

    this.courseService.updateCourse(this.course.id, course).pipe(
      switchMap(() => {
        return this.intakeService.linkCourseToIntake(this.course.id, intakeId);
      }),
      switchMap(() => this.courseService.getCourseListByPage())
    ).subscribe(pageResult => {
      this.showLoading = false;
      this.closeModal();
      this.courseOutput.emit(pageResult);
      this.toggle = false;
      this.toast.success('Môn học đã được cập nhật', 'Thành công');
    }, err => {
      this.showLoading = false;
      this.toast.error('Có lỗi xảy ra', 'Lỗi');
    });
  }

  closeModal() {
    this.showModalUpdate = false;
    this.toggle = false;
  }
}
