import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { BookService } from '../services/book-service';
import { Book, LoginResponse, Page, Tag } from '../../types/types';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-favorites',
  imports: [DecimalPipe],
  standalone: true,
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.css'
})
export class FavoritesComponent {
  currentUser: LoginResponse | null = null;
  
  api = inject(UserService);
  bookApi = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  books: Book[] = [];
  allBooks: Book[] = [];
  
  constructor(private authService: AuthService) { }
  

  ngOnInit(): void {
    this.loadBooks();

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });
  }

  sortedTags(tags: Tag[]): Tag[] {
    return [...tags].sort((a, b) => a.name.localeCompare(b.name));
  }

  loadBooks(): void {
    this.api.getFavorites().subscribe({
      next: (data: Book[]) => {
        this.allBooks = data;
        this.totalPages = Math.ceil(this.allBooks.length / this.pageSize);
        this.applyPage();
        this.cdr.detectChanges();
      },
    });
  }

  applyPage(): void {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.books = this.allBooks.slice(start, end);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.applyPage();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.applyPage();
    }
  }

  checkDetails(id: number) {
    this.router.navigate(['/books', id]);
  }

  deleteFavorite(bookId: number): void {
    if (!this.currentUser) return;

    this.bookApi.removeFromFavorites(bookId, this.currentUser.email).subscribe({
      next: () => {
        this.loadBooks();
      },
      error: (err) => {
        console.error('Fout bij verwijderen van favoriet:', err);
      }
    });
  }
}
