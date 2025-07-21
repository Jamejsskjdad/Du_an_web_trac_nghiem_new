import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  // true = đã thu gọn, false = mở rộng
  private _sidebarCollapsed = new BehaviorSubject<boolean>(false);
  sidebarCollapsed$ = this._sidebarCollapsed.asObservable();

  setSidebarCollapsed(val: boolean) {
    this._sidebarCollapsed.next(val);
  }

  getSidebarCollapsed() {
    return this._sidebarCollapsed.getValue();
  }
}
