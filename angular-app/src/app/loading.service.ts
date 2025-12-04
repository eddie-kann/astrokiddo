import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({providedIn: 'root'})
export class LoadingService {
  private counter = 0;
  private loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  show() {
    this.counter++;
    if (!this.loadingSubject.value) {
      this.loadingSubject.next(true);
    }
  }

  hide() {
    this.counter = Math.max(0, this.counter - 1);
    if (this.counter === 0 && this.loadingSubject.value) {
      this.loadingSubject.next(false);
    }
  }
}
