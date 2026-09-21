import { Routes } from '@angular/router';

import { Login } from './login/login';
import {Layout} from './layout/layout';
import {Home} from './home/home';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', component: Layout, children:[
      {path: 'home', component: Home},
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]}
];
