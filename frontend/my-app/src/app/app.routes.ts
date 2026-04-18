import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { BookListPage } from './book-pages/book-list-page/book-list-page';
import { BookDetailPage } from './book-pages/book-detail-page/book-detail-page';
import { AddBookPage } from './book-pages/add-book-page/add-book-page';
import { UpdateBookPage } from './book-pages/update-book-page/update-book-page';
import { BarcodePrintPage } from './book-pages/barcode-print-page/barcode-print-page';
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
import { LendingManagementComponent } from './lending-pages/lending-management/lending-management.component';
import { LendBookComponent } from './lending-pages/lend-book/lend-book.component';
import { ReturnBookComponent } from './lending-pages/return-book/return-book.component';
import { CollectionListPage } from './collection-pages/collection-list-page/collection-list-page';
import { CreateCollectionPage } from './collection-pages/create-collection-page/create-collection-page';
import { GlobalCollectionPage } from './collection-pages/global-collection-page/global-collection-page';
import { UpdateCollectionPage } from './collection-pages/update-collection-page/update-collection-page';

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
  { path: 'books/:id/barcodes', component: BarcodePrintPage, canActivate: [authGuard] },
  { path: 'barcodes', component: BarcodePrintPage, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard] },
  { path: 'favorites', component: FavoritesComponent, canActivate: [authGuard] },
  { path: 'tags', component: TagManagement, canActivate: [authGuard] },
  { path: 'lists', component: ReadingListPage, canActivate: [authGuard] },
  { path: 'lists/add', component: CreateReadingListPage, canActivate: [authGuard] },
  { path: 'lists/:id', component: ReadingListDetailPage, canActivate: [authGuard] },
  { path: 'lists/:id/edit', component: UpdateReadingListPage, canActivate: [authGuard] },
  { path: 'lendings', component: LendingManagementComponent, canActivate: [authGuard] },
  { path: 'lendings/lend', component: LendBookComponent, canActivate: [authGuard] },
  { path: 'lendings/return', component: ReturnBookComponent, canActivate: [authGuard] },
  { path: 'collections', component: CollectionListPage, canActivate: [authGuard] },
  { path: 'collections/add', component: CreateCollectionPage, canActivate: [authGuard] },
  { path: 'collections/global', component: GlobalCollectionPage, canActivate: [authGuard] },
  { path: 'collections/:id/edit', component: UpdateCollectionPage, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' },
];
