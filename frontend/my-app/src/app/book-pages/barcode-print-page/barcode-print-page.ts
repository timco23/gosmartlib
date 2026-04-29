import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BookService } from '../../services/book-service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { BarcodePrintItem, SchoolItemCopy } from '../../../types/types';

@Component({
  selector: 'app-barcode-print-page',
  imports: [FormsModule],
  templateUrl: './barcode-print-page.html',
  styleUrl: './barcode-print-page.css',
})
export class BarcodePrintPage implements OnInit, OnDestroy {
  private api = inject(BookService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private schoolService = inject(SchoolService);
  private cdr = inject(ChangeDetectorRef);

  items: BarcodePrintItem[] = [];
  bookTitles: Map<number, string> = new Map();
  loading = true;
  historyOpen = false;
  importHistory: { date: string; importedBooks: number; importedCopies: number; copyIds: number[] }[] = [];

  private objectUrls: string[] = [];

  ngOnInit(): void {
    this.importHistory = JSON.parse(localStorage.getItem('gosmartlib_barcode_history') ?? '[]');

    const params = this.route.snapshot.paramMap;
    const queryParams = this.route.snapshot.queryParamMap;
    const copyIds = queryParams.getAll('copyIds').map(Number);

    if (copyIds.length > 0) {
      // CSV import, load a specific selection of copies by their IDs
      this.loadCopiesByIds(copyIds);
    } else {
      // load all copies for a book
      const bookId = parseInt(params.get('id')!);
      const preselect = queryParams.get('preselect');
      const preselectAll = preselect === 'all';
      const preselectId = !preselectAll && preselect ? parseInt(preselect) : null;
      this.loadCopiesByBookId(bookId, preselectAll, preselectId);
    }
  }

  private loadCopiesByIds(ids: number[]): void {
    this.api.getCopiesByIds(ids).subscribe({
      next: (copies) => {
        this.items = copies.map((copy) => ({ copy, selected: true, imageUrl: null }));
        this.loading = false;
        this.cdr.detectChanges();
        this.fetchBarcodeImages();
        this.fetchBookTitles(copies);
      },
    });
  }

  private loadCopiesByBookId(
    bookId: number,
    preselectAll: boolean,
    preselectId: number | null = null,
  ): void {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe((userData) => {
        if (!userData.schoolName) return;

        this.schoolService.getByName(userData.schoolName).subscribe((school) => {
          this.api.getCopiesByBookId(bookId, school.id!).subscribe((copies) => {
            this.items = copies.map((copy) => ({
              copy,
              selected: preselectAll || copy.id === preselectId,
              imageUrl: null,
            }));
            this.loading = false;
            this.cdr.detectChanges();
            this.fetchBarcodeImages();
            this.fetchBookTitles(copies);
          });
        });
      });
    });
  }

  private fetchBarcodeImages(): void {
    this.items.forEach((item) => {
      this.api.getBarcodePng(item.copy.id).subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          this.objectUrls.push(url);
          item.imageUrl = url;
          this.cdr.detectChanges();
        },
      });
    });
  }

  private fetchBookTitles(copies: SchoolItemCopy[]): void {
    const uniqueItemIds = [...new Set(copies.map((copy) => copy.itemId))];
    if (uniqueItemIds.length === 0) return;
    forkJoin(uniqueItemIds.map((id) => this.api.getBookById(id))).subscribe({
      next: (books) => {
        books.forEach((book) => this.bookTitles.set(book.id!, book.title));
        this.cdr.detectChanges();
      },
    });
  }

  loadFromHistory(entry: { copyIds: number[] }): void {
    this.historyOpen = false;
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
    this.objectUrls = [];
    this.items = [];
    this.bookTitles = new Map();
    this.loading = true;
    this.cdr.detectChanges();
    this.loadCopiesByIds(entry.copyIds);
  }

  formatHistoryDate(iso: string): string {
    return new Date(iso).toLocaleString('nl-BE', { dateStyle: 'short', timeStyle: 'short' });
  }

  toggleAll(selected: boolean): void {
    this.items.forEach((item) => (item.selected = selected));
  }

  get selectedItems(): BarcodePrintItem[] {
    return this.items.filter((item) => item.selected);
  }

  print(): void {
    window.print();
  }

  back(): void {
    const bookId = this.route.snapshot.paramMap.get('id');
    if (bookId) {
      this.router.navigate(['/books', bookId]);
    } else {
      this.router.navigate(['/books/add']);
    }
  }

  ngOnDestroy(): void {
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
  }
}
