import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  SimpleChanges,
  effect,
  inject,
  model,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BookSelectorItem } from '../../../types/types';
import { BookService } from '../../services/book-service';
import { firstValueFrom, Observable } from 'rxjs';

@Component({
  selector: 'app-book-selector-popup-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './book-selector-popup-component.html',
  styleUrl: './book-selector-popup-component.css',
})
export class BookSelectorPopupComponent implements OnChanges, OnInit {
  private readonly bookService = inject(BookService);
  renderer = inject(Renderer2)

  isOpen = model<boolean>(false);
  @Input() books: BookSelectorItem[] = [];
  @Input() limit = 10;
  @Input() selectedId: string | number | null = null;

  @Input() getId: (book: BookSelectorItem) => string | number = (b) => b.id ?? b.bookId ?? '';
  @Input() getTitle: (book: BookSelectorItem) => string = (b) =>
    b.title ?? b.bookTitle ?? 'Onbekend boek';
  @Input() getMeta: (book: BookSelectorItem) => string = (b) =>
    [b.author ?? b.authorName, b.isbn].filter(Boolean).join(' • ');

  @Output() bookSelected = new EventEmitter<BookSelectorItem>();
  @Output() close = new EventEmitter<void>();

  searchTerm = '';
  filteredBooks: BookSelectorItem[] = [];

  private fallbackBooks: BookSelectorItem[] = [];
  private readonly pageSize = 200;
  private readonly maxPages = 50;


  //Locks scrolling for body of the entire page, not just this component.
  constructor() {
    effect(() => {
      if (this.isOpen()) {
        this.renderer.addClass(document.body, 'no-scroll');
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

  ngOnInit(): void {
    void this.loadFallbackBooks();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen()) {
      console.log("open");
      this.searchTerm = '';
    }
    if (changes['isOpen'] || changes['books']) {
      this.applyFilter();
    }
  }

  applyFilter(): void {
    const q = this.normalize(this.searchTerm);
    const source = this.books.length >= this.fallbackBooks.length ? this.books : this.fallbackBooks;

    const list = !q
      ? source
      : source.filter((b) => {
          const haystack = this.normalize(`${this.getTitle(b)} ${this.getMeta(b)}`);
          return haystack.includes(q);
        });

    this.filteredBooks = list.slice(0, this.limit);
  }

  isSelected(book: BookSelectorItem): boolean {
    if (this.selectedId === null || this.selectedId === undefined) return false;
    return String(this.getId(book)) === String(this.selectedId);
  }

  trackByBook = (_: number, book: BookSelectorItem): string | number => this.getId(book);

  private async loadFallbackBooks(): Promise<void> {
    try {
      const unique = new Map<string, BookSelectorItem>();

      for (let page = 0; page < this.maxPages; page++) {
        const response = await firstValueFrom(this.requestBooks(page, this.pageSize));
        const pageItems = this.extractItems(response);

        const before = unique.size;
        for (const item of pageItems) {
          const id = this.getId(item);
          const key = String(id || `${this.getTitle(item)}|${item.isbn ?? ''}`);
          unique.set(key, item);
        }
        const added = unique.size - before;

        const totalPages = this.extractTotalPages(response);
        if (totalPages !== null) {
          if (page + 1 >= totalPages) break;
        } else if (pageItems.length < this.pageSize || added === 0) {
          break;
        }
      }

      this.fallbackBooks = [...unique.values()];
      this.applyFilter();
    } catch {
      // silent fallback
    }
  }

  private requestBooks(page: number, size: number): Observable<unknown> {
    const svc = this.bookService as unknown as {
      getBooks: (...args: any[]) => Observable<unknown>;
    };
    return svc.getBooks(page, size);
  }

  private extractItems(payload: unknown): BookSelectorItem[] {
    if (Array.isArray(payload)) return payload as BookSelectorItem[];
    const wrapped = payload as { content?: unknown };
    if (Array.isArray(wrapped?.content)) return wrapped.content as BookSelectorItem[];
    return [];
  }

  private extractTotalPages(payload: unknown): number | null {
    const wrapped = payload as { totalPages?: unknown };
    return typeof wrapped?.totalPages === 'number' ? wrapped.totalPages : null;
  }

  private normalize(value: string | null | undefined): string {
    return (value ?? '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/\p{Diacritic}/gu, '')
      .trim();
  }

  closePopup() {
    this.isOpen.set(false);
    this.close.emit();
  }
}
