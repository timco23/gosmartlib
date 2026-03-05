import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { Login } from './login/login';
import { authGuard } from './guards/auth-guard';
import { Profile } from './profile/profile';
import { SchoolManagementComponent } from './school-management/school-management.component';
import { CreateUser } from './create-user/create-user';
import { ChangePassword } from './change-password/change-password';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard] },
  { path: 'create-user', component: CreateUser, canActivate: [authGuard] },
  { path: 'change-password', component: ChangePassword, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' },
];
