import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { BookService } from '../services/book-service';
import { Book, Page, Tag } from '../../types/types';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-favorites',
  imports: [DecimalPipe],
  standalone: true,
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.css'
})
export class FavoritesComponent {
  api = inject(UserService);
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
    this.api
      .getFavorites().subscribe({
        next: (data: Book[]) => {
          this.books = data;
          this.cdr.detectChanges();
        },
      });
  }

  sortedTags(tags: Tag[]): Tag[] {
    return [...tags].sort((a, b) => a.name.localeCompare(b.name));
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
