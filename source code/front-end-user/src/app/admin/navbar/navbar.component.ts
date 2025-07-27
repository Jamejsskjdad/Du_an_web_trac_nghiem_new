import {Component, OnInit} from '@angular/core';
import {TokenStorageService} from '../../_services/token-storage.service';
import {UserService} from '../../_services/user.service';
import {UserAccount} from '../../models/user-account';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class AdminNavbarComponent implements OnInit {
  toggleUserDropdown = false;
  avatarImg: string;
  user: UserAccount;

  constructor(private tokenStorageService: TokenStorageService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    const currentUser = this.tokenStorageService.getUser();
    this.userService.getUserInfo(currentUser.username).subscribe(res => {
      this.user = res.data;
      // ✅ Đảm bảo lấy icon mới nhất
    });
  }
  

  signOut() {
    this.tokenStorageService.signOut();
    window.location.reload();
  }


}
