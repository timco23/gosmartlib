import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Book, DidacticCategory, Genre, LoginResponse, Subgenre, Tag } from '../../types/types';
import { UserService } from '../services/user.service';
import { BookService } from '../services/book-service';
import { Router } from '@angular/router';
import { GenreService } from '../services/genre.service';
import { SubgenreService } from '../services/subgenre.service';
import { TagService } from '../services/tag.service';
import { DidacticCategoryService } from '../services/didactic-category.service';
import { AuthService } from '../services/auth.service';
import { PopUpComponent } from '../widgets/pop-up/pop-up.component';
import { StarDisplayComponent } from '../widgets/star-display/star-display.component';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-wishlist',
  imports: [PopUpComponent, StarDisplayComponent, DecimalPipe],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent {
currentUser: LoginResponse | null = null;
  showRemoveWishlistedPopup: boolean = false;
  pendingRemoveWishlistedId: number | null = null;
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
    this.api.getWishlistedBooks().subscribe({
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

  requestRemoveWishlisted(book: Book): void {
    if (!book?.id) return;
    this.pendingRemoveWishlistedId = book.id;
    this.showRemoveWishlistedPopup = true;
  }

  onRemoveWishlistedAction(value: string): void {
    if (value === 'confirm' && this.pendingRemoveWishlistedId) {
      this.deleteWishlisted(this.pendingRemoveWishlistedId);
    }
    this.showRemoveWishlistedPopup = false;
    this.pendingRemoveWishlistedId = null;
  }

  deleteWishlisted(bookId: number): void {
    if (!this.currentUser?.email) return;

    this.bookApi.removeFromWishlist(bookId, this.currentUser.email).subscribe({
      next: () => {
        this.loadBooks();
      },
      error: (err: any) => {
        console.error('Fout bij verwijderen van verlanglijst:', err);
      },
    });
  }

  coverSrc(book: any): string | null {
    const id = Number(book?.id);
    return Number.isFinite(id) && id > 0 ? this.bookApi.getBookCoverUrl(id) : null;
  }
}

