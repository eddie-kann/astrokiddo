import {Routes} from '@angular/router';
import {HomePageComponent} from './home-page/home-page.component';
import {DecksPageComponent} from './decks-page/decks-page.component';
import {AboutPageComponent} from './about-page/about-page.component';

export const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'home'},
  {path: 'home', component: HomePageComponent, title: 'AstroKiddo · Home'},
  {path: 'decks', component: DecksPageComponent, title: 'AstroKiddo · Lesson Decks'},
  {path: 'about', component: AboutPageComponent, title: 'AstroKiddo · About'},
  {path: '**', redirectTo: 'home'}
];

