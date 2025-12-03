import { provideZoneChangeDetection } from "@angular/core";
import {bootstrapApplication} from '@angular/platform-browser';
import {provideHttpClient} from '@angular/common/http';
import {provideRouter} from '@angular/router';
import {routes} from './app/app.routes';
import {AppComponent} from './app/app.component';
import 'zone.js';
import {provideAnimations} from '@angular/platform-browser/animations';
import {NZ_I18N, en_US} from 'ng-zorro-antd/i18n';
import {registerLocaleData} from '@angular/common';
import en from '@angular/common/locales/en';
import {provideNzIcons} from 'ng-zorro-antd/icon';
import {
  AppstoreOutline,
  AudioOutline,
  CalendarOutline,
  CloudDownloadOutline,
  FilePdfOutline,
  GithubOutline,
  HomeOutline,
  InfoCircleOutline,
  LoadingOutline,
  PauseOutline,
  PlayCircleOutline,
  RocketOutline,
  VideoCameraOutline,
  BookOutline,
  ArrowRightOutline
} from '@ant-design/icons-angular/icons';


registerLocaleData(en);

bootstrapApplication(AppComponent, {
  providers: [
    provideZoneChangeDetection(),provideHttpClient(),
    provideRouter(routes),
    provideAnimations(),
    {provide: NZ_I18N, useValue: en_US},
    provideNzIcons([
      AppstoreOutline,
      AudioOutline,
      CalendarOutline,
      CloudDownloadOutline,
      FilePdfOutline,
      GithubOutline,
      HomeOutline,
      InfoCircleOutline,
      LoadingOutline,
      PauseOutline,
      PlayCircleOutline,
      RocketOutline,
      VideoCameraOutline,
      BookOutline,
      ArrowRightOutline
    ])
  ]
})
  .catch(err => console.error(err));
