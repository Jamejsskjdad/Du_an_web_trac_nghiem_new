import {Component, OnInit} from '@angular/core';
import {TokenStorageService} from '../_services/token-storage.service';
import {UserService} from '../_services/user.service';
import {UserProfile} from '../models/user-profile';
import {Router} from '@angular/router';
import { SidebarService } from '../_services/sidebar.service';
@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {


  currentUser;
  userProfile: UserProfile;
  toggledMenu = false;

  constructor(
    private tokenStorageService: TokenStorageService,
    private userService: UserService,
    private router: Router,
    private sidebarService: SidebarService   // Inject service
  ) {}

  ngOnInit(): void {
    this.currentUser = this.tokenStorageService.getUser();
    this.userService.getUserInfo(this.currentUser.username).subscribe((res) => {
      this.userProfile = res.data.profile;
    });
    // Đồng bộ trạng thái sidebar với service
    this.sidebarService.sidebarCollapsed$.subscribe(val => {
      this.toggledMenu = val;
    });
  }

  signOut() {
    this.tokenStorageService.signOut();
    window.location.reload();
  }

  toggleMenu() {
    // Điều khiển sidebar qua service thay vì chỉ set biến local
    this.sidebarService.setSidebarCollapsed(!this.toggledMenu);
  }
}
