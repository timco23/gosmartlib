import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { SchoolManagementComponent } from './school-management/school-management.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'schoolbeheer', component: SchoolManagementComponent}
];
