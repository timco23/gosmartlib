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
import { FavoritesComponent } from './favorites/favorites.component';
import { OauthCallback } from './oauth-callback/oauth-callback';
import { ReadingListPage } from './reading-list-pages/reading-list-page/reading-list-page';
import { ReadingListDetailPage } from './reading-list-pages/reading-list-detail-page/reading-list-detail-page';
import { CreateReadingListPage } from './reading-list-pages/create-reading-list-page/create-reading-list-page';
import { UpdateReadingListPage } from './reading-list-pages/update-reading-list-page/update-reading-list-page';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'oauth-callback', component: OauthCallback },
  { path: 'oauth/callback', component: OauthCallback },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'books', component: BookListPage, canActivate: [authGuard] },
  { path: 'books/add', component: AddBookPage, canActivate: [authGuard] },
  { path: 'books/:id', component: BookDetailPage, canActivate: [authGuard] },
  { path: 'books/:id/edit', component: UpdateBookPage, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard] },
  { path: 'favorites', component: FavoritesComponent, canActivate: [authGuard] },
  { path: 'tags', component: TagManagement, canActivate: [authGuard] },
  { path: 'lists', component: ReadingListPage, canActivate: [authGuard] },
  { path: 'lists/:id', component: ReadingListDetailPage, canActivate: [authGuard] },
  { path: 'lists/add', component: CreateReadingListPage, canActivate: [authGuard] },
  { path: 'lists/:id/edit', component: UpdateReadingListPage, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' },
];
