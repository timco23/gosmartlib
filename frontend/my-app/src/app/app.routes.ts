import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { Login } from './login/login';

export const routes: Routes = [
    { path: 'login', component: Login },
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'home', component: HomeComponent }
];
