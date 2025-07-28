import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../_services/user.service';
import { ToastrService } from 'ngx-toastr';
import { UserAccount } from '../../../models/user-account';
import { UserProfile } from '../../../models/user-profile';
import { PageResult } from '../../../models/page-result';
import { switchMap } from 'rxjs/operators';
import { UserUpdate } from '../../../models/user-update';

@Component({
  selector: 'app-update-user',
  templateUrl: './update-user.component.html',
  styleUrls: ['./update-user.component.scss']
})
export class UpdateUserComponent implements OnInit {

  @Input() userInfo: UserAccount;
  @Output() usersOutput = new EventEmitter<PageResult<UserAccount>>();

  showModalUpdate = false;
  rfUpdateUser: FormGroup;
  showLoading = false;
  pageResult: PageResult<UserAccount>;
  user: UserAccount;
  today: Date = new Date(); // dùng để giới hạn ngày sinh

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toast: ToastrService
  ) {}

  ngOnInit(): void {
    this.user = { ...this.userInfo };
    this.initForm();
  }

  get username() { return this.rfUpdateUser.get('username'); }
  get firstName() { return this.rfUpdateUser.get('firstName'); }
  get lastName() { return this.rfUpdateUser.get('lastName'); }
  get birthDate() { return this.rfUpdateUser.get('birthDate'); }
  get password() { return this.rfUpdateUser.get('password'); }
  get confirmPass() { return this.rfUpdateUser.get('confirmPass'); }

  toggleModalUpdate() {
    this.user = { ...this.userInfo };
    this.initForm();
    this.showModalUpdate = !this.showModalUpdate;
  }

  initForm() {
    this.rfUpdateUser = this.fb.group({
      username: [{ value: this.user.username, disabled: true }],
      firstName: [this.user.profile?.firstName || '', Validators.required],
      lastName: [this.user.profile?.lastName || '', Validators.required],
      birthDate: [this.user.birthDate || null],
      password: [null, Validators.pattern('^(?=.*[\\d])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*])[\\w!@#$%^&*]{8,}$')],
      confirmPass: [null]
    }, { validator: this.passwordConfirming });
  }

  passwordConfirming(c: AbstractControl): { invalid: boolean } | null {
    const pwd = c.get('password')?.value;
    const confirm = c.get('confirmPass')?.value;
    if (pwd && confirm && pwd !== confirm) {
      return { invalid: true };
    }
    return null;
  }

  onSubmit() {
    const profile = new UserProfile(this.firstName.value, this.lastName.value);

    const userUpdate: UserUpdate = {
      username: this.username.value,
      password: this.password.value,
      profile,
      birthDate: this.birthDate.value || null
    };

    this.showLoading = true;

    this.userService.updateUser(this.userInfo.id, userUpdate)
      .pipe(switchMap(() => this.userService.getUserList(0, 20)))
      .subscribe({
        next: (res) => {
          this.showLoading = false;
          this.closeModal();
          this.pageResult = res;
          this.usersOutput.emit(this.pageResult);
          this.showSuccess();
        },
        error: (err) => {
          this.showLoading = false;
          this.toast.error(err?.error?.message || 'Cập nhật thất bại!', 'Lỗi');
        }
      });
  }

  closeModal() {
    this.showModalUpdate = false;
  }

  private showSuccess() {
    this.toast.success('Người dùng đã được cập nhật!', 'Thành công');
  }
}
