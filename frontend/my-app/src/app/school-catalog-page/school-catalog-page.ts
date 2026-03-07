import { AsyncPipe } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';
import { Observable } from 'rxjs';
import { Book, Page } from '../../types/types';

@Component({
  selector: 'app-school-catalog-page',
  imports: [RouterLink],
  templateUrl: './school-catalog-page.html',
  styleUrl: './school-catalog-page.css',
})
export class SchoolCatalogPage {
  api = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  currentPage: number = 0;
  pageSize: number = 10;
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
