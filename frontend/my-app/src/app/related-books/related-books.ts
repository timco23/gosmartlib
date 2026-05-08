import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  inject,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BookService } from '../services/book-service';
import { SchoolService } from '../services/school.service';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';
import { GenreService } from '../services/genre.service';
import { Book, Genre } from '../../types/types';

type RelatedTab = 'all' | 'author' | 'tags' | 'genre';

@Component({
  selector: 'app-related-books',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './related-books.html',
  styleUrl: './related-books.css',
})
export class RelatedBooks implements OnChanges {
  @Input() book: Book | null = null;

  private api = inject(BookService);
  private schoolService = inject(SchoolService);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private genreService = inject(GenreService);
  private cdr = inject(ChangeDetectorRef);

  activeTab: RelatedTab = 'all';
  relatedBooks: Book[] = [];
  loading = false;

  currentPage = 0;
  totalPages = 0;
  pageSize = 5;

  private schoolId: number | null = null;
  allGenres: Genre[] = [];

  tabs: { key: RelatedTab; label: string }[] = [
    { key: 'all', label: 'Alle' },
    { key: 'author', label: 'Auteur' },
    { key: 'tags', label: 'Leefwereldlabels' },
    { key: 'genre', label: 'Genre' },
  ];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['book'] && this.book) {
      this.currentPage = 0;
      this.activeTab = 'all';
      this.loadSchoolAndBooks();
    }
  }

  private loadSchoolAndBooks(): void {
    if (this.allGenres.length === 0) {
      this.genreService.getGenres().subscribe((g) => {
        this.allGenres = g;
      });
    }

    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;
      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (data.schoolName) {
            this.schoolService.getByName(data.schoolName).subscribe({
              next: (school) => {
                this.schoolId = school.id ?? null;
                this.loadRelated();
              },
            });
          }
        },
      });
    });
  }

  selectTab(tab: RelatedTab): void {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadRelated();
  }

  loadRelated(): void {
    if (!this.book || !this.schoolId) return;

    this.loading = true;
    this.relatedBooks = [];

    const authorFilter = this.activeTab === 'author' ? this.book.author : '';
    const tagIds =
      this.activeTab === 'tags' || this.activeTab === 'all'
        ? (this.book.tags ?? []).map((t) => t.id)
        : [];
    const genreIds =
      this.activeTab === 'genre' || this.activeTab === 'all' ? (this.book.genres ?? []) : [];

    const search = this.activeTab === 'author' ? this.book.author : '';

    this.api
      .getBooksPaginated(
        this.currentPage,
        this.pageSize,
        'title',
        'asc',
        search,
        [],
        this.schoolId,
        false,
        undefined,
        undefined,
        undefined,
        this.activeTab === 'tags' || this.activeTab === 'all' ? tagIds : [],
        this.activeTab === 'genre' || this.activeTab === 'all' ? genreIds : [],
        [],
        [],
        undefined,
      )
      .subscribe({
        next: (data) => {
          // exclude the current book
          this.relatedBooks = (data.content ?? []).filter((b) => b.id !== this.book!.id);
          this.totalPages = data.totalPages ?? 0;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadRelated();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadRelated();
    }
  }

  getGenreNames(ids: number[]): string {
    return (ids ?? [])
      .map((id) => this.allGenres.find((g) => g.id === id)?.name)
      .filter(Boolean)
      .join(', ');
  }

  getCoverUrl(id: number): string {
    return this.api.getBookCoverUrl(id);
  }
}
