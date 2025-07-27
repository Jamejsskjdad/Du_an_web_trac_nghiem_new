import { Component, OnInit } from '@angular/core';
import { UserAccount } from '../../models/user-account';
import { UserService } from '../../_services/user.service';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TokenStorageService } from '../../_services/token-storage.service';

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.scss']
})
export class AdminProfileComponent implements OnInit {

  togglePwd = false;
  toggleEmail = false;
  toggleAvatar = true;

  userProfile: UserAccount;
  iconList: string[] = [
    'avt1.png',
    'avt2.png',
    'avt3.png',
    'avt4.png',
    'avt5.png',
    'avt6.png',
    'avt7.png',
    'avt8.png'
  ];
  selectedIcon: string = 'avt1.png';
  localizedRole: string = '';

  constructor(
    private userService: UserService,
    private fb: FormBuilder,
    private toast: ToastrService,
    private tokenStorageService: TokenStorageService
  ) {}

  ngOnInit(): void {
    this.getUserInfo();
  }

  getUserInfo() {
    this.userService.getUserInfo('').subscribe(res => {
      this.userProfile = res.data;
  
      // Nếu đã có icon từ backend thì dùng
      if (this.userProfile.profile?.icon) {
        this.selectedIcon = this.userProfile.profile.icon;
      } else {
        // Nếu chưa có thì dùng icon mặc định (không gọi API)
        this.selectedIcon = 'avt1.png';
      }     
    const roleName = this.userProfile.roles?.[0]?.name || '';
    this.localizedRole = this.getLocalizedRole(roleName);
    });
  }

  toggleChangePwd() {
    this.toggleEmail = false;
    this.toggleAvatar = false;
    this.togglePwd = true;
  }

  toggleUpdateEmail() {
    this.togglePwd = false;
    this.toggleAvatar = false;
    this.toggleEmail = true;
  }

  toggleChangeAvatar() {
    this.togglePwd = false;
    this.toggleEmail = false;
    this.toggleAvatar = true;
  }

  updatePassword(data) {
    this.userService.updatePassword(this.userProfile.id, data).subscribe(res => {
      if (res.statusCode === 200) {
        this.toast.success(res.message, 'Done');
        this.toast.warning(`You have to sign in again`, 'Warning');
        setTimeout(() => {
          this.tokenStorageService.signOut();
          window.location.replace('/login');
        }, 3000);
      } else {
        this.toast.error(res.message, 'Error');
      }
    }, error => {
      this.toast.error(error.message, `Server error: ${error.statusCode}`);
    });
  }

  updateEmail(data) {
    this.userService.updateEmail(this.userProfile.id, data).subscribe(res => {
      switch (res.statusCode) {
        case 417:
          this.toast.error('Error', res.message);
          break;
        case 200:
          this.toast.success('Done', res.message);
          this.userProfile.email = res.data;
          break;
        default:
          this.toast.error('Error', 'Server error');
      }
    });
  }

  changeIcon(icon: string) {
    this.selectedIcon = icon;
    this.userService.updateUserIcon(this.userProfile.profile.id, icon).subscribe(
      res => {
        this.toast.success('Đã cập nhật ảnh đại diện!', 'Thành công');

        // ✅ Cập nhật localStorage và đồng bộ với navbar
        const updatedUser = this.tokenStorageService.getUser();
        if (updatedUser.profile) {
          updatedUser.profile.icon = icon;
          this.tokenStorageService.saveUser(updatedUser);
        }
      },
      err => {
        this.toast.error('Lỗi cập nhật ảnh đại diện!', 'Thất bại');
      }
    );
  }
  getLocalizedRole(roleName: string): string {
    switch (roleName) {
      case 'ROLE_ADMIN':
        return 'Quản trị viên';
      case 'ROLE_TEACHER':
        return 'Giáo viên';
      case 'ROLE_STUDENT':
        return 'Học viên';
      default:
        return 'Người dùng';
    }
  }
  
}
