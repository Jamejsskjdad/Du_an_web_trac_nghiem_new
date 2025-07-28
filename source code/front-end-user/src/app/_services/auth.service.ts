import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { Router } from '@angular/router';

const AUTH_API = 'http://localhost:8080/api/auth';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(
    private http: HttpClient,
    private tokenStorageService: TokenStorageService,
    private router: Router
  ) {}

  login(credentials: { username: string, password: string }): Observable<any> {
    return this.http.post(AUTH_API + '/signin', credentials, httpOptions);
  }

  goLogin() {
    this.router.navigate(['/login']).then((e) => {
      if (e) {
        console.log('on login');
      } else {
        console.log('fail');
      }
    });
  }
}
