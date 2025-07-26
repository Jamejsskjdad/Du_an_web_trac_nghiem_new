import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CourseService} from '../../../_services/course.service';
import {Course} from '../../../models/course';
import {switchMap} from 'rxjs/operators';
import {ToastrService} from 'ngx-toastr';
import {PageResult} from '../../../models/page-result';
import {UploadFileService} from '../../../_services/upload-file.service';
import {Intake} from '../../../models/intake'; // đã có rồi
import {IntakeService} from '../../../_services/intake.service'; // cần tạo file này
@Component({
  selector: 'app-course-create',
  templateUrl: './course-create.component.html',
  styleUrls: ['./course-create.component.scss']
})
export class CourseCreateComponent implements OnInit {

  showModal = false;
  rfCreateCourse: FormGroup;
  @Output() outputCourse = new EventEmitter<PageResult<Course>>();
  // selectedFiles: FileList;
  // currentFileUpload: File;
  intakeList: Intake[] = [];
  selectedIntakeId: number;
  constructor(private fb: FormBuilder,
              private courseService: CourseService,
              private toast: ToastrService,
              private uploadFileService: UploadFileService,
              private intakeService: IntakeService) {
  }

  get courseCode() {
    return this.rfCreateCourse.get('courseCode');
  }

  get courseName() {
    return this.rfCreateCourse.get('courseName');
  }

  ngOnInit(): void {
    this.rfCreateCourse = this.fb.group({
      courseCode: ['', {
        validators: [Validators.required],
        asyncValidators: [this.courseService.validateCourseCode()],
        updateOn: 'blur'
      }],
      courseName: ['', Validators.required],
      intakeId: ['', Validators.required]
    });
    this.intakeService.getIntakeList().subscribe(data => this.intakeList = data);
  }
  

  toggleModalAdd() {
    this.showModal = !this.showModal;
    this.rfCreateCourse.reset();
  }

  onSubmit() {
    const intakeId = this.rfCreateCourse.get('intakeId').value;
    const course: Course = new Course(this.courseCode.value, this.courseName.value);
    this.courseService.createCourse(course).pipe(switchMap(res => {
      const courseId = res?.data?.id;
      return this.intakeService.linkCourseToIntake(courseId, intakeId);
    })).pipe(switchMap(() => this.courseService.getCourseListByPage()))
      .subscribe(res => {
        this.closeModal();
        this.toast.success('Môn học mới đã được tạo', 'Thành công');
        this.outputCourse.emit(res);
      }, error => {
        this.toast.error('Đã có vấn đề xảy ra', 'Lỗi');
      });
    
  }
  

  closeModal() {
    this.showModal = false;
  }

}
