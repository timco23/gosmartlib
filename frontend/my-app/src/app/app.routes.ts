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
import { CreateUser } from './create-user/create-user';
import { ChangePassword } from './change-password/change-password';
import { FavoritesComponent } from './favorites/favorites.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'books', component: BookListPage },
  { path: 'books/add', component: AddBookPage },
  { path: 'books/:id', component: BookDetailPage },
  { path: 'books/:id/edit', component: UpdateBookPage },
  { path: 'login', component: Login },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'schools', component: SchoolManagementComponent },
  { path: 'tags', component: TagManagement },
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard] },
  { path: 'create-user', component: CreateUser, canActivate: [authGuard] },
  { path: 'change-password', component: ChangePassword, canActivate: [authGuard] },
  { path: 'favorites', component: FavoritesComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' },
];
