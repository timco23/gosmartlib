import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { BookService } from '../../services/book-service';
import { AsyncPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Book, Page } from '../../../types/types';

@Component({
  selector: 'app-book-list-page',
  imports: [RouterLink],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.css',
})
export class BookListPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  currentPage: number = 0;
  pageSize: number = 20;
  totalPages: number = 0;
  books: Book[] = [];

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.api.getBooksPaginated(this.currentPage, this.pageSize).subscribe({
      next: (data: Page<Book>) => {
        this.books = data.content;
        this.totalPages = data.totalPages;
        this.cdr.detectChanges();
      },
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadBooks();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadBooks();
    }
  }

  checkDetails(id: number) {
    this.router.navigate(['/books', id]);
  }
}
