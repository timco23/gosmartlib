import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { BookListPage } from './book-pages/book-list-page/book-list-page';
import { BookDetailPage } from './book-pages/book-detail-page/book-detail-page';
import { AddBookPage } from './book-pages/add-book-page/add-book-page';
import { UpdateBookPage } from './book-pages/update-book-page/update-book-page';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'books', component: BookListPage },
    { path: 'books/add', component: AddBookPage },
    { path: 'books/:id' , component: BookDetailPage },
    { path: 'books/:id/edit', component: UpdateBookPage }
];
