import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../_services/user.service';
import { UserAccount } from '../../../models/user-account';
import { UserProfile } from '../../../models/user-profile';
import { PageResult } from '../../../models/page-result';
import { ToastrService } from 'ngx-toastr';
import { UploadFileService } from '../../../_services/upload-file.service';
import { IntakeService } from '../../../_services/intake.service';
import { Intake } from '../../../models/intake';

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
  openTab = 1;
  pageResult: PageResult<UserAccount>;
  userExcelFile: any;
  intakes: Intake[] = [];
  userImportSuccess: UserAccount[] = [];
  userTotal: number;
  today: Date = new Date(); // ✅ Dùng để giới hạn ngày sinh

  constructor(
    private userService: UserService,
    private fb: FormBuilder,
    private toast: ToastrService,
    private uploadFileService: UploadFileService,
    private intakeService: IntakeService
  ) {}

  get username() {
    return this.rfAddUser.get('username');
  }

  get firstName() {
    return this.rfAddUser.get('firstName');
  }

  get lastName() {
    return this.rfAddUser.get('lastName');
  }

  get birthDate() {
    return this.rfAddUser.get('birthDate');
  }

  ngOnInit(): void {
    this.intakeService.getIntakeList().subscribe(data => {
      this.intakes = data;
    });

    this.rfAddUser = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(6)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      birthDate: [null], // ✅ Cho phép null
      roleId: [1, Validators.required],
      intakeId: [null]
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
    const intakeId = this.rfAddUser.get('intakeId').value;
    const birthDateValue = this.birthDate.value;

    const user: any = {
      username: this.username.value,
      profile,
      password,
      roleId,
      birthDate: this.birthDate.value ? new Date(this.birthDate.value).toISOString() : null, // ✅ FIXED
      intakeId: roleId === 3 ? intakeId : null
    };

    this.userService.addUser(user).subscribe((res: any) => {
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
    this.toast.success('Người dùng đã được tạo mới!', 'Thành công');
  }

  toggleTabs($tabNumber: number) {
    this.openTab = $tabNumber;
  }

  importExcelUser() {
    this.uploadFileService.uploadUsersByExcel(this.userExcelFile).subscribe(res => {
      this.userImportSuccess = res.data;
      this.userTotal = res.userTotal;
      this.toast.success('Đã import danh sách user', 'Thành công');
    });
  }

  getExcel(file) {
    this.userExcelFile = file;
  }
}
