import {
  Component,
  effect,
  EventEmitter,
  inject,
  Input,
  model,
  OnInit,
  Output,
  Renderer2,
} from '@angular/core';
import { BookService } from '../../services/book-service';
import { Book } from '../../../types/types';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, Observable } from 'rxjs';

@Component({
  selector: 'app-select-book-popup',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './select-book-popup.html',
  styleUrl: './select-book-popup.css',
})
export class SelectBookPopup implements OnInit {
  shown = model<boolean>(false);
  itemId = model<number>(0);

  @Output() addBookEvent = new EventEmitter<void>();

  private readonly bookService = inject(BookService);
  private readonly renderer = inject(Renderer2);

  books: Book[] = [];
  filteredBooks: Book[] = [];
  searchTerm = '';
  selectedBookId: number | null = null;

  resultLimit = 10;
  loadingBooks = false;
  loadBooksError = false;
  invalidChoiceError = false;

  private readonly pageSize = 200;
  private readonly maxPages = 50;

  constructor() {
    effect(() => {
      if (this.shown()) {
        this.renderer.addClass(document.body, 'no-scroll');
        this.invalidChoiceError = false;
        this.searchTerm = '';
        this.selectedBookId = this.itemId() > 0 ? this.itemId() : null;
        this.applyFilter();
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

  ngOnInit(): void {
    void this.loadAllBooks();
  }

  closePopup(): void {
    this.shown.set(false);
  }

  chooseBook(book: Book): void {
    this.selectedBookId = book.id;
    this.itemId.set(book.id);
    this.invalidChoiceError = false;
  }

  confirmSelection(): void {
    this.invalidChoiceError = false;

    if (!this.selectedBookId || this.selectedBookId === 0) {
      this.invalidChoiceError = true;
      return;
    }

    this.itemId.set(this.selectedBookId);
    this.addBookEvent.emit();
    this.closePopup();
  }

  applyFilter(): void {
    const q = this.normalize(this.searchTerm);

    const list = !q
      ? this.books
      : this.books.filter((b) => {
          const title = this.titleOf(b);
          const author = this.authorOf(b);
          const haystack = this.normalize(`${title} ${author} ${b.isbn ?? ''}`);
          return haystack.includes(q);
        });

    this.filteredBooks = list.slice(0, this.resultLimit);
  }

  bookMeta(book: Book): string {
    return [this.authorOf(book), book.isbn].filter(Boolean).join(' • ');
  }

  private async loadAllBooks(): Promise<void> {
    this.loadingBooks = true;
    this.loadBooksError = false;

    try {
      const unique = new Map<number, Book>();

      for (let page = 0; page < this.maxPages; page++) {
        const response = await firstValueFrom(this.requestBooks(page, this.pageSize));
        const pageBooks = this.extractBooks(response);

        const before = unique.size;
        for (const b of pageBooks) {
          if (b?.id != null) unique.set(b.id, b);
        }
        const added = unique.size - before;

        const totalPages = this.extractTotalPages(response);
        if (totalPages !== null) {
          if (page + 1 >= totalPages) break;
        } else if (pageBooks.length < this.pageSize || added === 0) {
          break;
        }
      }

      this.books = [...unique.values()].sort((a, b) =>
        this.titleOf(a).localeCompare(this.titleOf(b)),
      );
      this.applyFilter();
    } catch {
      this.loadBooksError = true;
    } finally {
      this.loadingBooks = false;
    }
  }

  private requestBooks(page: number, size: number): Observable<unknown> {
    const svc = this.bookService as unknown as {
      getBooks: (...args: any[]) => Observable<unknown>;
    };
    return svc.getBooks(page, size);
  }

  private extractBooks(payload: unknown): Book[] {
    if (Array.isArray(payload)) return payload as Book[];
    const wrapped = payload as { content?: unknown };
    if (Array.isArray(wrapped?.content)) return wrapped.content as Book[];
    return [];
  }

  private extractTotalPages(payload: unknown): number | null {
    const wrapped = payload as { totalPages?: unknown };
    return typeof wrapped?.totalPages === 'number' ? wrapped.totalPages : null;
  }

  private titleOf(book: Book): string {
    const b = book as Book & { bookTitle?: string };
    return (b.title ?? b.bookTitle ?? '').trim();
  }

  private authorOf(book: Book): string {
    const b = book as Book & { authorName?: string };
    return (b.author ?? b.authorName ?? '').trim();
  }

  private normalize(value: string | null | undefined): string {
    return (value ?? '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/\p{Diacritic}/gu, '')
      .trim();
  }
}
