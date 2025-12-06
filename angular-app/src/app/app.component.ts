import {Component} from '@angular/core';

import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {NzLayoutModule} from 'ng-zorro-antd/layout';
import {NzMenuModule} from 'ng-zorro-antd/menu';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {AsyncPipe} from '@angular/common';
import {LoadingService} from './loading.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NzLayoutModule, NzMenuModule, NzIconModule, NzButtonModule, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  navLinks = [
    {label: 'Home', path: '/home', icon: 'home'},
    {label: 'Lesson Decks', path: '/decks', icon: 'appstore'},
    {label: 'About', path: '/about', icon: 'info-circle'}
  ];

  externalLinks = [
    {label: 'NASA', href: 'https://apod.nasa.gov/apod/astropix.html', icon: 'rocket'},
    {label: 'University', href: 'https://www.nasa.gov/learning-resources/', icon: 'book'},
    {label: 'GitHub', href: 'https://github.com/eddie-kann/astrokiddo', icon: 'github'}
  ];

  readonly loading$;

  constructor(private loadingSvc: LoadingService) {
    this.loading$ = this.loadingSvc.loading$.pipe(
      distinctUntilChanged(),
      debounceTime(0)
    );
  }
}
