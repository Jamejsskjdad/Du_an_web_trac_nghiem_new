import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from '../_services/auth.service';
import {TokenStorageService} from '../_services/token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private authService: AuthService, private router: Router, private tokenStorageService: TokenStorageService) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
  
    const currentUser = this.tokenStorageService.getUser();
  
    if (currentUser) {
      const userRoles = currentUser.roles; // mảng ví dụ: ['ROLE_ADMIN', 'ROLE_STUDENT']
      const requiredRoles = next.data.roles; // mảng yêu cầu từ routing
  
      // Nếu route yêu cầu quyền và người dùng không có quyền nào khớp
      if (requiredRoles && !requiredRoles.some(role => userRoles.includes(role))) {
        this.router.navigate(['/']);
        return false;
      }
  
      return true;
    }
  
    // Chưa đăng nhập
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
  

  // tslint:disable-next-line:max-line-length
  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.canActivate(childRoute, state);
  }
}
