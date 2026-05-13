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
import { TagManagement } from './widgets/tag-management/tag-management.component';
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
import { ForbiddenComponent } from './forbidden-component/forbidden-component';
import { ADMIN_ONLY, ALL_USERS, LIBRARIAN_PLUS, TEACHER_PLUS } from './guards/roles';
import { LendingHistoryComponent } from './lending-history/lending-history.component';
import { DashboardComponent } from './dashboard-component/dashboard-component';
import { BookLendingHistoryComponent } from './book-lending-history/book-lending-history.component';
import { ProfileSelectionComponent } from './profile-selection/profile-selection';
import { DidacticBooksPage } from './didactic-books-page/didactic-books-page';
import { WishlistComponent } from './wishlist/wishlist.component';
import { BookReportPage } from './book-pages/book-report-page/book-report-page';
import { ClassStatisticsPage } from './class-statistics-page/class-statistics-page';
import { StudentStatisticsPage } from './student-statistics-page/student-statistics-page';
import { ClassListPage } from './class-list-page/class-list-page';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'oauth-callback', component: OauthCallback },
  { path: 'forbidden', component: ForbiddenComponent },


  { path: 'books/add', component: AddBookPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'books/:id/edit', component: UpdateBookPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'books/:id/barcodes', component: BarcodePrintPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },

  // LEERLING +
  { path: 'home', component: HomeComponent, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'books', component: BookListPage, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'books/:id', component: BookDetailPage, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'favorites', component: FavoritesComponent, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'profile', component: Profile, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'lendingHistory', component: LendingHistoryComponent, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'wishlist', component: WishlistComponent, canActivate: [authGuard], data: { roles: ALL_USERS } },

  // LEERKRACHT +
  { path: 'books/:id/report', component: BookReportPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'class/:classId/statistics', component: ClassStatisticsPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'class/:classId/student/:userId/statistics', component: StudentStatisticsPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'statistics/class/:classId', component: ClassStatisticsPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'classes', component: ClassListPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'lists', component: ReadingListPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'lists/add', component: CreateReadingListPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'lists/:id', component: ReadingListDetailPage, canActivate: [authGuard], data: { roles: ALL_USERS } },
  { path: 'lists/:id/edit', component: UpdateReadingListPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },
  { path: 'didactic-books', component: DidacticBooksPage, canActivate: [authGuard], data: { roles: TEACHER_PLUS } },

  // BIBLIOTHEEKBEHEERDER +
  { path: 'books/add', component: AddBookPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'books/:id/edit', component: UpdateBookPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'books/:id/barcodes', component: BarcodePrintPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'barcodes', component: BarcodePrintPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'lendings', component: LendingManagementComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'lendings/lend', component: LendBookComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'lendings/return', component: ReturnBookComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'collections', component: CollectionListPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'collections/add', component: CreateCollectionPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'collections/global', component: GlobalCollectionPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'collections/:id/edit', component: UpdateCollectionPage, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'school-settings', component: SchoolManagementComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'book-lending-history/:bookId', component: BookLendingHistoryComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },
  { path: 'accounts', component: ProfileSelectionComponent, canActivate: [authGuard], data: { roles: LIBRARIAN_PLUS } },

  // ADMIN
  { path: 'schools', component: SchoolManagementComponent, canActivate: [authGuard], data: { roles: ADMIN_ONLY } },
  { path: 'schools/add', component: SchoolManagementComponent, canActivate: [authGuard], data: { roles: ADMIN_ONLY } },
  { path: 'schools/:id/edit', component: SchoolManagementComponent, canActivate: [authGuard], data: { roles: ADMIN_ONLY } },
  { path: 'tags', component: TagManagement, canActivate: [authGuard], data: { roles: ADMIN_ONLY } },

  // alias voor typo /school
  { path: 'school', component: SchoolManagementComponent, canActivate: [authGuard], data: { roles: ADMIN_ONLY } },

  { path: '**', redirectTo: 'login' },
];