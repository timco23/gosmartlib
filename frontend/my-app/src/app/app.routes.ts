import { Routes } from '@angular/router';
import { HomeComponent } from './home-component/home-component';
import { BookListPage } from './book-list-page/book-list-page';
import { BookDetailPage } from './book-detail-page/book-detail-page';
import { AddBookPage } from './add-book-page/add-book-page';
import { UpdateBookPage } from './update-book-page/update-book-page';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'books', component: BookListPage },
    { path: 'book/:id' , component: BookDetailPage },
    { path: 'books/add', component: AddBookPage },
    { path: 'books/:id/edit', component: UpdateBookPage }
];
