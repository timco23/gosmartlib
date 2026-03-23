import { DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, Page, Role, Tag } from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { TagSelectionMenu } from '../../selection-menu-pages/tag-selection-menu/tag-selection-menu';
import { AuthService } from '../../services/auth.service';
import { TagService } from '../../services/tag.service';

@Component({
  selector: 'app-book-list-page',
  imports: [RouterLink, FormsModule, DecimalPipe, TagSelectionMenu],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.css',
})
export class BookListPage implements OnInit {
  api = inject(BookService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);
  authService = inject(AuthService)
  role = Role;

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
  selectedTags: Tag[] = [];

  allTags: Tag[] = [];
  tagService = inject(TagService);

  ngOnInit(): void {
    this.tagService.getTags().subscribe(tags => {
      this.allTags = tags;
    });
    this.loadBooks();
  }

  loadBooks(): void {
    const tagIds = this.selectedTags.map((t) => t.id!);
    console.log('tagIds:', tagIds);
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
        tagIds,
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
  sortedTags(tagIds: number[]): Tag[] {
    return tagIds
      .map(id => this.allTags.find(t => t.id === id))
      .filter((t): t is Tag => !!t)
      .sort((a, b) => a.name.localeCompare(b.name));
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

  onResetFilters(): void {
    this.search = '';
    this.languages = ['Nederlands', 'Engels', 'Frans', 'Duits'];
    this.minPages = undefined;
    this.maxPages = undefined;
    this.minYear = undefined;
    this.maxYear = undefined;
    this.minScore = undefined;
    this.selectedTags = [];
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
