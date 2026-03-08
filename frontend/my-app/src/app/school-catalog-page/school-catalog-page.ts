import { AsyncPipe } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';
import { Observable } from 'rxjs';
import { Book, Page } from '../../types/types';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-school-catalog-page',
  imports: [RouterLink, FormsModule],
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
  sortBy: string = 'title';
  sortDirection: string = 'asc';
  search: string = '';
  languages: string[] = ['Nederlands', 'Engels', 'Frans', 'Duits'];
  minPages?: number;
  maxPages?: number;
  minYear?: number;
  maxYear?: number;
  minScore?: number;

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.api
      .getBooksPaginated(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
        this.search,
        this.languages,
        this.minPages,
        this.maxPages,
        this.minYear,
        this.maxYear,
        this.minScore,
      )
      .subscribe({
        next: (data: Page<Book>) => {
          this.books = data.content;
          this.totalPages = data.totalPages;
          this.cdr.detectChanges();
        },
      });
  }

  onSortChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    const [sortBy, sortDir] = value.split('-');
    this.sortBy = sortBy;
    this.sortDirection = sortDir;
    this.currentPage = 0;
    this.loadBooks();
  }

  onSearchChange(value: string): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  onLanguageChange(language: string, checked: boolean): void {
    if (checked) {
      this.languages = [...this.languages, language];
    } else {
      this.languages = this.languages.filter((l) => l !== language);
    }
    this.currentPage = 0;
    this.loadBooks();
  }

  onSliderChange(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  onResetFilters(): void {
    this.search = '';
    this.languages = ['Nederlands', 'Engels', 'Frans', 'Duits'];
    this.minPages = undefined;
    this.maxPages = undefined;
    this.minYear = undefined;
    this.maxYear = undefined;
    this.minScore = undefined;
    this.currentPage = 0;
    this.loadBooks();
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
