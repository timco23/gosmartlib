import { Component, inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { BookService } from '../../services/book-service';
import { AsyncPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Book } from '../../../types/types';

@Component({
  selector: 'app-book-list-page',
  imports: [AsyncPipe, RouterLink],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.css',
})
export class BookListPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);

  bookList: Observable<Book[]> | null = null;

  ngOnInit(): void {
    this.bookList = this.api.getBooks();
  }

  checkDetails(id:number) {
    this.router.navigate(["/books", id]);
  }

}
