import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { BookService } from '../services/book-service';
import { Book, DidacticCategory, Genre, LoginResponse, Subgenre, Tag } from '../../types/types';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';
import { GenreService } from '../services/genre.service';
import { SubgenreService } from '../services/subgenre.service';
import { TagService } from '../services/tag.service';
import { DidacticCategoryService } from '../services/didactic-category.service';
import { StarDisplayComponent } from '../widgets/star-display/star-display.component';
import { PopUpComponent } from '../widgets/pop-up/pop-up.component';

@Component({
  selector: 'app-favorites',
  imports: [DecimalPipe, StarDisplayComponent, PopUpComponent],
  standalone: true,
  templateUrl: './favorites.component.html',
  styleUrl: './favorites.component.css',
})
export class FavoritesComponent {
  currentUser: LoginResponse | null = null;
  showRemoveFavoritePopup: boolean = false;
  pendingRemoveFavoriteId: number | null = null;
  loading: boolean = true;

  api = inject(UserService);
  bookApi = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  genreService = inject(GenreService);
  subgenreService = inject(SubgenreService);
  tagService = inject(TagService);
  didacticCategoryService = inject(DidacticCategoryService);

  currentPage: number = 0;
  pageSize: number = 15;
  totalPages: number = 0;
  books: Book[] = [];
  allBooks: Book[] = [];

  allGenres: Genre[] = [];
  allSubgenres: Subgenre[] = [];
  allTags: Tag[] = [];
  allDidacticCategories: DidacticCategory[] = [];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.genreService.getGenres().subscribe((g) => {
      this.allGenres = g;
      this.cdr.detectChanges();
    });
    this.subgenreService.getSubgenres().subscribe((s) => {
      this.allSubgenres = s;
      this.cdr.detectChanges();
    });
    this.tagService.getTags().subscribe((t) => {
      this.allTags = t;
      this.cdr.detectChanges();
    });
    this.didacticCategoryService.getDidacticCategories().subscribe((c) => {
      this.allDidacticCategories = c;
      this.cdr.detectChanges();
    });

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    this.loadBooks();
  }

  sortedTags(tags: Tag[]): Tag[] {
    return [...tags].sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedGenres(ids: number[]): Genre[] {
    return (ids || [])
      .map((id) => this.allGenres.find((g) => g.id === id)!)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedSubgenres(ids: number[]): Subgenre[] {
    return (ids || [])
      .map((id) => this.allSubgenres.find((s) => s.id === id)!)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedDidacticCategories(ids: number[]): DidacticCategory[] {
    return (ids || [])
      .map((id) => this.allDidacticCategories.find((c) => c.id === id)!)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  loadBooks(): void {
    this.api.getFavorites().subscribe({
      next: (data: Book[]) => {
        this.allBooks = data.sort((a, b) => a.title.localeCompare(b.title));
        this.totalPages = Math.ceil(this.allBooks.length / this.pageSize);
        this.applyPage();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
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

  requestRemoveFavorite(book: Book): void {
    if (!book?.id) return;
    this.pendingRemoveFavoriteId = book.id;
    this.showRemoveFavoritePopup = true;
  }

  onRemoveFavoriteAction(value: string): void {
    if (value === 'confirm' && this.pendingRemoveFavoriteId) {
      this.deleteFavorite(this.pendingRemoveFavoriteId);
    }
    this.showRemoveFavoritePopup = false;
    this.pendingRemoveFavoriteId = null;
  }

  deleteFavorite(bookId: number): void {
    if (!this.currentUser?.email) return;

    this.bookApi.removeFromFavorites(bookId, this.currentUser.email).subscribe({
      next: () => {
        this.loadBooks();
      },
      error: (err) => {
        console.error('Fout bij verwijderen van favoriet:', err);
      },
    });
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookApi.getBookCoverUrl(id) : null;
  }
}
