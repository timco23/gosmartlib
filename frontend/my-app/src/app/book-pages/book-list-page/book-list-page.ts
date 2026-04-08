import { DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import {
  Book,
  DidacticCategory,
  Genre,
  Page,
  Role,
  School,
  Subgenre,
  Tag,
} from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { TagSelectionMenu } from '../../selection-menu-pages/tag-selection-menu/tag-selection-menu';
import { AuthService } from '../../services/auth.service';
import { SchoolService } from '../../services/school.service';
import { UserService } from '../../services/user.service';
import { TagService } from '../../services/tag.service';
import { GenreSelectionMenuComponent } from '../../selection-menu-pages/genre-selection-menu/genre-selection-menu.component';
import { DidacticCategorySelectionMenuComponent } from '../../selection-menu-pages/didactic-category-selection-menu/didactic-category-selection-menu.component';
import { SubgenreSelectionMenuComponent } from '../../selection-menu-pages/subgenre-selection-menu/subgenre-selection-menu.component';

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
  role = Role;

  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  books: Book[] = [];
  sortBy: string = 'title';
  sortDirection: string = 'asc';
  search: string = '';
  languages: string[] = [];
  minPages?: number;
  maxPages?: number;
  minScore?: number;
  selectedTags: Tag[] = [];
  school: School | null = null;

  allTags: Tag[] = [];
  tagService = inject(TagService);

  selectedGenres: Genre[] = [];
  selectedSubgenres: Subgenre[] = [];
  selectedCategories: DidacticCategory[] = [];
  didactic: boolean | undefined = undefined;

  get canUseDidacticFilters(): boolean {
    return this.authService.atLeastHasRole(this.role.LEERKRACHT);
  }

  ngOnInit(): void {
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

    const tagIds = this.selectedTags.map((t) => t.id!);
    const genreIds = this.selectedGenres.map((g) => g.id!);
    const subgenreIds = this.selectedSubgenres.map((s) => s.id!);

    const effectiveDidactic = this.canUseDidacticFilters ? this.didactic : false;
    const didacticCategoryIds =
      effectiveDidactic === true ? this.selectedCategories.map((c) => c.id!) : [];

    this.api
      .getBooksPaginated(
        this.currentPage,
        this.pageSize,
        this.sortBy,
        this.sortDirection,
        this.search,
        this.languages,
        this.school.id,
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
  //sort tags alphabetically
  sortedTags(tags: Tag[]): Tag[] {
    return tags.slice().sort((a, b) => a.name.localeCompare(b.name));
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

  onTagsChanged(tags: Tag[]): void {
    console.log('tags changed:', tags);
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

  checkDetails(id: number) {
    this.router.navigate(['/books', id]);
  }
}
