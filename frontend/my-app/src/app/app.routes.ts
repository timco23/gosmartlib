import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { Login } from './login/login';
import { authGuard } from './guards/auth-guard';
import { Profile } from './pages/profile/profile';
import { SchoolManagementComponent } from './school-management/school-management.component';
import { TagManagement } from './tag-management/tag-management';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'schoolbeheer', component: SchoolManagementComponent },
  { path: 'tags', component: TagManagement },
];
