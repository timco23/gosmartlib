import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { BookListPage } from './book-pages/book-list-page/book-list-page';
import { BookDetailPage } from './book-pages/book-detail-page/book-detail-page';
import { AddBookPage } from './book-pages/add-book-page/add-book-page';
import { UpdateBookPage } from './book-pages/update-book-page/update-book-page';
import { Login } from './login/login';
import { authGuard } from './guards/auth-guard';
import { Profile } from './profile/profile';
import { SchoolManagementComponent } from './school-management/school-management.component';
import { TagManagement } from './tag-management/tag-management.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'books', component: BookListPage, canActivate: [authGuard] },
  { path: 'books/add', component: AddBookPage, canActivate: [authGuard] },
  { path: 'books/:id', component: BookDetailPage, canActivate: [authGuard] },
  { path: 'books/:id/edit', component: UpdateBookPage, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard] },
  { path: 'tags', component: TagManagement, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' },
];
