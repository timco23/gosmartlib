import { Component, inject } from '@angular/core';
import { OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Book, GlobalBook } from '../models/book-model';
import { BookService } from '../services/book-service';
import { AsyncPipe } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-book-list-page',
  imports: [AsyncPipe],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.css',
})
export class BookListPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);

  bookList: Observable<Book[]> | null = null;
  globalBookList: Observable<GlobalBook[]> | null = null;


  ngOnInit(): void {
    this.bookList = this.api.getBooks();
    this.globalBookList = this.api.getGlobalBooks();
  }

  checkDetails(id:number) {
    this.router.navigate(["/books", id]);
  }

}
