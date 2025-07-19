import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../../_services/user.service';
import {UserAccount} from '../../../models/user-account';
import {UserProfile} from '../../../models/user-profile';
import {PageResult} from '../../../models/page-result';
import {switchMap} from 'rxjs/operators';
import {ToastrService} from 'ngx-toastr';
import {FileUploadComponent} from '../../../shared/file-upload/file-upload.component';
import {UploadFileService} from '../../../_services/upload-file.service';
import {IntakeService} from '../../../_services/intake.service';
import {Intake} from '../../../models/intake';

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.scss']
})
export class AddUserComponent implements OnInit {
  showModalAdd = false;
  rfAddUser: FormGroup;
  @Output() usersAddOutput = new EventEmitter<PageResult<UserAccount>>();
  @Input() active = false;
  @Input() tabTitle: string;
  // @ViewChild(FileUploadComponent) fileUpload: FileUploadComponent;
  openTab = 1;
  pageResult: PageResult<UserAccount>;
  userExcelFile: any;
  intakes: Intake[] = [];

  userImportSuccess: UserAccount[] = [];
  userTotal: number;

  constructor(private userService: UserService,
              private fb: FormBuilder,
              private toast: ToastrService,
              private uploadFileService: UploadFileService, private intakeService: IntakeService) {
  }

  get username() {
    return this.rfAddUser.get('username');
  }

  get firstName() {
    return this.rfAddUser.get('firstName');
  }

  get lastName() {
    return this.rfAddUser.get('lastName');
  }

  get email() {
    return this.rfAddUser.get('email');
  }

  ngOnInit(): void {
    this.intakeService.getIntakeList().subscribe(data => {
      this.intakes = data;
    });
  
    // Bỏ dòng reset cũ (nó không cần thiết ở đây)
    // this.rfAddUser?.reset(this.rfAddUser.value);
  
    this.rfAddUser = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$')
      ]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      roleId: [1, Validators.required],
      intakeId: [null]  // Thêm dòng này!
    });
    
  }
  

  closeModal() {
    this.showModalAdd = false;
    this.rfAddUser.reset();
    this.openTab = 1;
    this.userTotal = 0;
  }

  onSubmit() {
    const profile = new UserProfile(this.firstName.value, this.lastName.value);
    const roleId = this.rfAddUser.get('roleId').value;
    const password = this.rfAddUser.get('password').value;
    const intakeId = this.rfAddUser.get('intakeId').value; // lấy intakeId nếu là student

    // Debug password
    console.log("Password khi tạo user:", password);

    const user: any = {
      username: this.username.value,
      email: this.email.value,
      profile,
      password,
      roleId,
      intakeId: roleId == 3 ? intakeId : null // chỉ gửi intakeId nếu là học viên
    };
    // Debug:
    console.log(user);
  
          this.userService.addUser(user).subscribe((res: any) => {
        // Log password gốc và hash trả về từ backend
        console.log("Password gốc:", res.data.rawPassword);
        console.log("Password hash:", res.data.passwordHash);

        // Sau khi tạo xong, lấy lại danh sách user
        this.userService.getUserList(0, 20).subscribe(res2 => {
          this.closeModal();
          this.showSuccess();
          this.pageResult = res2;
          this.usersAddOutput.emit(this.pageResult);
        });
      });
  }
  
  
  toggleModalAdd() {
    this.showModalAdd = !this.showModalAdd;
  }

  showSuccess() {
    this.toast.success('Người dùng đã được tạo mới!', 'Thành công',);
  }

  toggleTabs($tabNumber: number) {
    this.openTab = $tabNumber;
  }

  importExcelUser() {
    this.uploadFileService.uploadUsersByExcel(this.userExcelFile).subscribe(res => {
      this.userImportSuccess = res.data;
      this.userTotal = res.userTotal;
      console.log(this.userImportSuccess);
      this.toast.success('Đã import danh sách user', 'Thành công');
    });
  }

  getExcel(file) {
    this.userExcelFile = file;
  }
}
