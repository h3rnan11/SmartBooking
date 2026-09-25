import { Routes } from '@angular/router';

import { Login } from './login/login';
import {Layout} from './layout/layout';
import {Home} from './home/home';
import { authGuard, guestGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: '', component: Layout, canActivate: [authGuard], canActivateChild: [authGuard], children:[
      {path: 'home', component: Home},
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]},
  { path: '**', redirectTo: '' }
];
