import { DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { AuthService } from '../../services/auth.service';
import { SchoolService } from '../../services/school.service';
import { UserService } from '../../services/user.service';
import { TagService } from '../../services/tag.service';
import { GenreService } from '../../services/genre.service';
import { SubgenreService } from '../../services/subgenre.service';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { TagSelectionMenu } from '../../selection-menu-pages/tag-selection-menu/tag-selection-menu';
import { GenreSelectionMenuComponent } from '../../selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { SubgenreSelectionMenuComponent } from '../../selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';
import { DidacticCategorySelectionMenuComponent } from '../../selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { StarDisplayComponent } from '../../star-display/star-display.component';
import {
  Book,
  DidacticCategory,
  Genre,
  Page,
  Readinglevel,
  Role,
  School,
  Subgenre,
  Tag,
} from '../../../types/types';
import { ReadingListSlideOutPopUp } from '../reading-list-slide-out-pop-up/reading-list-slide-out-pop-up';

@Component({
  selector: 'app-book-list-page',
  imports: [
    RouterLink,
    FormsModule,
    DecimalPipe,
    TagSelectionMenu,
    GenreSelectionMenuComponent,
    DidacticCategorySelectionMenuComponent,
    SubgenreSelectionMenuComponent,
    ReadingListSlideOutPopUp,
    StarDisplayComponent,
  ],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.css',
})
export class BookListPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  authService = inject(AuthService);
  private schoolService = inject(SchoolService);
  private userService = inject(UserService);
  private tagService = inject(TagService);
  private genreService = inject(GenreService);
  private subgenreService = inject(SubgenreService);
  private didacticCategoryService = inject(DidacticCategoryService);
  private readingLevelService = inject(ReadingLevelService);

  role = Role;

  currentPage = 0;
  pageSize = 15;
  totalPages = 0;

  books: Book[] = [];
  sortBy = 'title';
  sortDirection = 'asc';
  search = '';
  languages: string[] = [];
  minPages?: number;
  maxPages?: number;
  minScore?: number;

  selectedTags: Tag[] = [];
  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedCategories: DidacticCategory[] = [];
  didactic: boolean | undefined = undefined;

  selectedReadingLevel = '';
  readingLevels: Readinglevel[] = [];

  school: School | null = null;
  getSchoolSpecificBooks = true;

  allTags: Tag[] = [];
  allGenres: Genre[] = [];
  allSubgenres: Subgenre[] = [];
  allDidacticCategories: DidacticCategory[] = [];
  booksToBeAddedToReadingList: Book[] = [];
  showReadingListPopUp: Boolean = false;

  get canUseDidacticFilters(): boolean {
    return this.authService.atLeastHasRole(this.role.LEERKRACHT);
  }

  get visibleBooks(): Book[] {
    if (!this.selectedReadingLevel) return this.books;

    const selectedId = Number(this.selectedReadingLevel);
    const selected = this.readingLevels.find((l) => l.id === selectedId);
    const selectedName = (selected?.name ?? '').trim().toLowerCase();

    return this.books.filter((book) => {
      const level = this.extractBookReadingLevel(book);

      if (Number.isFinite(selectedId) && level.id !== null && level.id === selectedId) {
        return true;
      }

      if (selectedName && level.name && level.name.toLowerCase() === selectedName) {
        return true;
      }

      return false;
    });
  }

  ngOnInit(): void {
    this.loadReadingLevels();

    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (data.schoolName != null) {
            this.schoolService.getByName(data.schoolName).subscribe({
              next: (schoolData) => {
                this.school = schoolData;

                this.tagService.getTags().subscribe((tags) => {
                  this.allTags = tags;
                  this.cdr.detectChanges();
                });

                this.genreService.getGenres().subscribe((g) => {
                  this.allGenres = g;
                  this.cdr.detectChanges();
                });

                this.subgenreService.getSubgenres().subscribe((s) => {
                  this.allSubgenres = s;
                  this.cdr.detectChanges();
                });

                this.didacticCategoryService.getDidacticCategories().subscribe((c) => {
                  this.allDidacticCategories = c;
                  this.cdr.detectChanges();
                });

                this.loadBooks();
              },
            });
          }

          if (!this.canUseDidacticFilters) {
            this.didactic = false;
            this.selectedCategories = [];
          }
        },
      });
    });
  }

  loadBooks(): void {
    if (!this.school?.id) return;

    const tagIds = this.selectedTags.map((t) => t.id);
    const genreIds = this.selectedGenres.map((g) => g.id);
    const subgenreIds = this.selectedSubgenres.map((s) => s.id);

    const effectiveDidactic = this.canUseDidacticFilters ? this.didactic : false;
    const didacticCategoryIds =
      effectiveDidactic === true ? this.selectedCategories.map((c) => c.id) : [];

    this.api
      .getBooksPaginated(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
        this.search,
        this.languages,
        this.school.id,
        this.getSchoolSpecificBooks,
        this.minPages,
        this.maxPages,
        this.minScore,
        tagIds,
        genreIds,
        subgenreIds,
        didacticCategoryIds,
        effectiveDidactic,
      )
      .subscribe({
        next: (data: Page<Book>) => {
          this.books = data.content ?? [];
          this.totalPages = data.totalPages ?? 0;
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

  sortedTags(tags: Tag[]): Tag[] {
    return (tags ?? []).slice().sort((a, b) => a.name.localeCompare(b.name));
  }

  onSearchChange(_: string): void {
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

  onReadingLevelChange(level: string | number): void {
    this.selectedReadingLevel = String(level ?? '');
  }

  onSliderChange(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  onTagsChanged(tags: Tag[]): void {
    this.selectedTags = tags;
    this.currentPage = 0;
    this.loadBooks();
  }

  onGenresChanged(genres: Genre[]): void {
    this.selectedGenres = genres;
    this.currentPage = 0;
    this.loadBooks();
  }

  onSubgenresChanged(subgenres: Subgenre[]): void {
    this.selectedSubgenres = subgenres;
    this.currentPage = 0;
    this.loadBooks();
  }

  onDidacticFilterChange(value: boolean | undefined): void {
    if (!this.canUseDidacticFilters) return;

    this.didactic = value;
    if (value !== true) {
      this.selectedCategories = [];
    }

    this.currentPage = 0;
    this.loadBooks();
  }

  onDidacticTagsChanged(didacticTags: DidacticCategory[]): void {
    if (!this.canUseDidacticFilters || this.didactic !== true) return;

    this.selectedCategories = didacticTags;
    this.currentPage = 0;
    this.loadBooks();
  }

  onResetFilters(): void {
    this.search = '';
    this.languages = [];
    this.minPages = undefined;
    this.maxPages = undefined;
    this.minScore = undefined;
    this.selectedTags = [];
    this.selectedGenres = [];
    this.selectedSubgenres = [];
    this.selectedCategories = [];
    this.selectedReadingLevel = '';
    this.didactic = this.canUseDidacticFilters ? undefined : false;
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

  checkDetails(id: number): void {
    this.router.navigate(['/books', id]);
  }

  toggleSchoolSpecificBooks(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  getSortedGenres(ids: number[]): Genre[] {
    return (ids || [])
      .map((id) => this.allGenres.find((g) => g.id === id)!)
      .filter(Boolean)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedSubgenres(ids: number[]): Subgenre[] {
    return (ids || [])
      .map((id) => this.allSubgenres.find((s) => s.id === id)!)
      .filter(Boolean)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  getSortedDidacticCategories(ids: number[]): DidacticCategory[] {
    return (ids || [])
      .map((id) => this.allDidacticCategories.find((c) => c.id === id)!)
      .filter(Boolean)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  private loadReadingLevels(): void {
    this.readingLevelService.getAll().subscribe({
      next: (levels) => {
        this.readingLevels = (levels ?? [])
          .map((item: any) => {
            const id = Number(item?.id);
            const name = String(item?.name ?? item?.level ?? '').trim();
            const description = String(item?.description ?? '').trim();

            if (!Number.isFinite(id) || !name) return null;

            return { id, name, description } as Readinglevel;
          })
          .filter((x): x is Readinglevel => x !== null)
          .sort((a, b) => a.name.localeCompare(b.name));

        this.cdr.detectChanges();
      },
      error: () => {
        this.readingLevels = [];
        this.cdr.detectChanges();
      },
    });
  }

  private extractBookReadingLevel(book: Book): { id: number | null; name: string | null } {
    const b: any = book as any;
    const raw =
      b.readingLevel ?? b.readinglevel ?? b.readingLevelId ?? b.leesniveau ?? b.level ?? null;

    if (raw === null || raw === undefined) {
      return { id: null, name: null };
    }

    if (typeof raw === 'number') {
      return { id: Number.isFinite(raw) ? raw : null, name: null };
    }

    if (typeof raw === 'string') {
      const trimmed = raw.trim();
      if (!trimmed) return { id: null, name: null };

      const parsed = Number(trimmed);
      if (Number.isFinite(parsed)) {
        return { id: parsed, name: null };
      }

      return { id: null, name: trimmed };
    }

    if (typeof raw === 'object') {
      const idRaw = Number(raw.id ?? raw.readingLevelId ?? raw.value ?? NaN);
      const nameRaw = String(raw.name ?? raw.level ?? raw.label ?? '').trim();

      return {
        id: Number.isFinite(idRaw) ? idRaw : null,
        name: nameRaw || null,
      };
    }

    return { id: null, name: null };
  }

  addBookToReadingList(book: Book) {
    if (!this.booksToBeAddedToReadingList.includes(book)) {
      this.booksToBeAddedToReadingList.push(book);
    }
    else {
      this.showReadingListPopUp = true;
    }
  }
}
