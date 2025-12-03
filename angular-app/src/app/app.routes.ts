import {Routes} from '@angular/router';
import {AboutComponent} from './about/about.component';
import {DecksComponent} from './decks/decks.component';
import {HomeComponent} from './home/home.component';

export const routes: Routes = [
  {path: '', component: HomeComponent, pathMatch: 'full'},
  {path: 'decks', component: DecksComponent},
  {path: 'about', component: AboutComponent},
  {path: '**', redirectTo: ''}
];

