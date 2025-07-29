import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../_services/auth.service';
import { TokenStorageService } from '../_services/token-storage.service';
import { UserRole } from '../models/user-role.enum';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  roles: string[] = [];
  preLoading = false;
  returnUrl: string;
  openTab = 1;

  showPassword = false; // ✅ Thêm biến để điều khiển hiển thị mật khẩu

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private authService: AuthService,
    private tokenStorageService: TokenStorageService,
    private fb: FormBuilder,
    private toast: ToastrService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.isLoggedIn = !!this.tokenStorageService?.getToken();
    this.returnUrl = this.activatedRoute.snapshot.queryParams['returnUrl'] || '/';

    if (this.isLoggedIn) {
      const user = this.tokenStorageService.getUser();
      this.roles = user.roles;
      if (this.roles.includes(UserRole.ROLE_ADMIN)) {
        this.toLogin(UserRole.ROLE_ADMIN);
      } else if (this.roles.includes(UserRole.ROLE_LECTURER)) {
        this.toLogin(UserRole.ROLE_LECTURER);
      } else if (this.roles.includes(UserRole.ROLE_STUDENT)) {
        this.toLogin(UserRole.ROLE_STUDENT);
      }
    }
  }

  toLogin(role: string) {
    switch (role) {
      case UserRole.ROLE_STUDENT:
        this.router.navigate(['/user/dashboard']);
        break;
      case UserRole.ROLE_ADMIN:
        this.router.navigate(['/admin/dashboard']);
        break;
      case UserRole.ROLE_LECTURER:
        this.router.navigate(['/lecturer/dashboard']);
        break;
    }
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.preLoading = true;
    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe(
      data => {
        this.tokenStorageService.saveToken(data.accessToken);
        this.tokenStorageService.saveUser(data);

        this.isLoginFailed = false;
        this.isLoggedIn = true;

        this.roles = this.tokenStorageService.getUser().roles;
        if (this.roles.includes(UserRole.ROLE_ADMIN)) {
          this.toLogin(UserRole.ROLE_ADMIN);
        } else if (this.roles.includes(UserRole.ROLE_STUDENT)) {
          this.toLogin(UserRole.ROLE_STUDENT);
        } else if (this.roles.includes(UserRole.ROLE_LECTURER)) {
          this.toLogin(UserRole.ROLE_LECTURER);
        }
      },
      err => {
        this.errorMessage = err?.message || 'Lỗi đăng nhập';
        this.isLoginFailed = true;
        this.preLoading = false;
      }
    );
  }

  togglePassword() {
    this.showPassword = !this.showPassword; // ✅ Hàm đổi trạng thái hiển thị mật khẩu
  }
}
