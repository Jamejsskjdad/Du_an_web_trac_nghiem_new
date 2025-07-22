import { AfterContentInit, AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { UserService } from '../../_services/user.service';
import { UserAccount } from '../../models/user-account';
import { PaginationDetail } from '../../models/pagination/pagination-detail';
import { delay, switchMap } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { IntakeService } from '../../_services/intake.service';

@Component({
  selector: 'app-manage-user',
  templateUrl: './manage-user.component.html',
  styleUrls: ['./manage-user.component.scss']
})
export class ManageUserComponent implements OnInit, AfterContentInit {

  userList: UserAccount[] = [];
  paginationDetail: PaginationDetail;
  skeleton = true;
  pageOptions: any = [
    { display: 20, num: 20 },
    { display: 50, num: 50 },
    { display: 100, num: 100 },
    { display: 'Tất cả', num: '' },
  ];
  searchKeyWord = '';
  pageCountShowing = 20;
  selectedIntakeId: number | null = null;
  intakeList: any[] = [];
  constructor(
    private userService: UserService,
    private intakeService: IntakeService,
    private toast: ToastrService
  ) { }



  ngOnInit(): void {
    this.intakeService.getIntakeList().subscribe({
      next: (data) => this.intakeList = data,
      error: () => this.toast.error('Không lấy được danh sách lớp!')
    });
    this.fetchUserList();
  }


  fetchUserList() {
    // this.userService.searchUserListDeletedByPage(0, 20, this.searchKeyWord, false).subscribe(res => {
    //   console.table(res);
    //   this.userList = res.data;
    //   this.paginationDetail = res.paginationDetails;
    // }, error => {
    //   console.error('Lỗi');
    // });
    this.userService.getUserList(0, 20).subscribe(res => {
      this.userList = res.data;
      this.paginationDetail = res.paginationDetails;
      this.skeleton = false;
    });
  }

  trackById(index: number, item) {
    return item.id;
  }

  exportUserToExcel() {
    this.userService.exportExcel(false).subscribe(blob => {
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = 'users.xlsx';
      link.click();
      window.URL.revokeObjectURL(link.href);
    }, err => {
      // Có thể hiện toast báo lỗi nếu cần
      this.toast.error('Không thể xuất file!');
    });
  }


  goPreviousPage() {
    const isFirstPage: boolean = this.paginationDetail.isFirstPage;
    if (!isFirstPage) {
      this.userService.searchUserList(this.paginationDetail.previousPage.pageNumber, this.pageCountShowing, this.searchKeyWord)
        .subscribe(res => {
          this.userList = res.data;
          this.paginationDetail = res.paginationDetails;
          console.table(this.userList);
        });
    }

  }

  goNextPage() {
    const isLastPage = !this.paginationDetail.nextPage.available;
    if (!isLastPage) {
      this.userService.searchUserList(this.paginationDetail.nextPage.pageNumber, this.pageCountShowing, this.searchKeyWord
      ).subscribe(res => {
        this.userList = res.data;
        this.paginationDetail = res.paginationDetails;
        console.table(this.paginationDetail);
      });
    }
  }

  fetchUsersAfterCRUD($event: any) {
    this.userList = $event.data;
    this.paginationDetail = $event.paginationDetails;
  }

  changePageShow(value: any) {
    this.pageCountShowing = value;
    if (!this.pageCountShowing) {
      this.userService.getUserList(0, this.paginationDetail.totalCount).subscribe(res => {
        this.userList = res.data;
        this.paginationDetail = res.paginationDetails;
      });
    } else {
      this.userService.getUserList(0, this.pageCountShowing).subscribe(res => {
        this.userList = res.data;
        this.paginationDetail = res.paginationDetails;
      });
    }
  }

  searchUser(searchKeyWord: string) {
    console.log(searchKeyWord);
    this.userService.searchUserList(0, 20, searchKeyWord).subscribe(res => {
      this.userList = res.data;
      this.paginationDetail = res.paginationDetails;
    });
  }

  ngAfterContentInit(): void {
    console.log('after view init');
  }

  changeDeleted(user: UserAccount) {
    user.deleted = !user.deleted;
    this.userService.deleteUser(user.id, user.deleted)
      .pipe(switchMap(res => this.userService.getUserList(0, 20)))
      .subscribe(res => {
        this.toast.success('Đã thay đổi trạng thái tài khoản', 'Thành công');
      }, error => {
        user.deleted = !user.deleted;
        this.toast.error('Không thể thay đổi trạng thái', 'Lỗi');
      });
  }
  onExportPasswords() {
    if (!this.selectedIntakeId) {
      this.toast.error('Bạn phải chọn lớp trước!');
      return;
    }
    this.userService.generatePasswordsExcel(this.selectedIntakeId).subscribe(blob => {
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = 'student-passwords.xlsx';
      link.click();
      window.URL.revokeObjectURL(link.href);
    });
  }
  onUpdatePasswords(file: File) {
    if (!file) {
      this.toast.error('Bạn cần chọn file!');
      return;
    }
    const formData = new FormData();
    formData.append('file', file);
    this.userService.updatePasswordsFromExcel(formData).subscribe(
      res => {
        this.toast.success(res || 'Cập nhật thành công!');
      },
      err => {
        this.toast.error('Lỗi cập nhật mật khẩu!');
      }
    );
  }



  showForm = false;

  toggleForm() {
    this.showForm = !this.showForm;
  }


}
