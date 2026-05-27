import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BookService } from '../../services/book-service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import {
  BarcodePrintItem,
  BulkImportHistoryEntry,
  ConfirmDialogAction,
  SchoolItemCopy,
  SchoolItemCopyExcelPayload,
} from '../../../types/types';

@Component({
  selector: 'app-barcode-print-page',
  imports: [FormsModule, PopUpComponent],
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
  importHistory: BulkImportHistoryEntry[] = [];
  undoError: string | null = null;
  confirmUndoEntry: BulkImportHistoryEntry | null = null;
  readonly undoActions: ConfirmDialogAction[] = [
    { label: 'Annuleren', type: 'cancel', value: 'cancel' },
    { label: 'Ongedaan maken', type: 'danger', value: 'confirm' },
  ];

  private schoolId: number | null = null;
  private objectUrls: string[] = [];

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe((userData) => {
        if (!userData.schoolName) return;

        this.schoolService.getByName(userData.schoolName).subscribe((school) => {
          this.schoolId = school.id!;
          this.loadHistory();

          const params = this.route.snapshot.paramMap;
          const queryParams = this.route.snapshot.queryParamMap;
          const copyIds = queryParams.getAll('copyIds').map(Number);

          if (copyIds.length > 0) {
            this.loadCopiesByIds(copyIds);
          } else {
            const bookId = parseInt(params.get('id')!);
            const preselect = queryParams.get('preselect');
            const preselectAll = preselect === 'all';
            const preselectId = !preselectAll && preselect ? parseInt(preselect) : null;
            this.loadCopiesByBookId(bookId, school.id!, preselectAll, preselectId);
          }
        });
      });
    });
  }

  private loadHistory(): void {
    if (this.schoolId === null) return;
    this.api.getImportHistory(this.schoolId).subscribe({
      next: (history) => {
        this.importHistory = history;
        this.cdr.detectChanges();
      },
    });
  }

  private loadCopiesByIds(ids: number[]): void {
    this.api.getCopiesByIds(ids).subscribe({
      next: (copies) => {
        this.items = copies
          .filter((copy) => copy.copy_status !== 'KWIJT')
          .map((copy) => ({ copy, selected: true, imageUrl: null }));
        this.loading = false;
        this.cdr.detectChanges();
        this.fetchBarcodeImages();
        this.fetchBookTitles(copies);
      },
    });
  }

  private loadCopiesByBookId(
    bookId: number,
    schoolId: number,
    preselectAll: boolean,
    preselectId: number | null = null,
  ): void {
    this.api.getCopiesByBookId(bookId, schoolId).subscribe((copies) => {
      this.items = copies
        .filter((copy) => copy.copy_status !== 'KWIJT')
        .map((copy) => ({
          copy,
          selected: preselectAll || copy.id === preselectId,
          imageUrl: null,
        }));
      this.loading = false;
      this.cdr.detectChanges();
      this.fetchBarcodeImages();
      this.fetchBookTitles(copies);
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

  loadFromHistory(entry: BulkImportHistoryEntry): void {
    this.historyOpen = false;
    this.undoError = null;
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
    this.objectUrls = [];
    this.items = [];
    this.bookTitles = new Map();
    this.loading = true;
    this.cdr.detectChanges();
    this.loadCopiesByIds(entry.copyIds);
  }

  undoImport(entry: BulkImportHistoryEntry): void {
    this.undoError = null;
    this.confirmUndoEntry = entry;
  }

  onUndoAction(value: string): void {
    const entry = this.confirmUndoEntry!;
    this.confirmUndoEntry = null;
    if (value !== 'confirm') return;

    this.api.undoImport(entry.id).subscribe({
      next: () => {
        const entryIds = new Set(entry.copyIds);
        const viewingThisImport = this.items.some((i) => entryIds.has(i.copy.id));
        if (viewingThisImport) {
          this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
          this.objectUrls = [];
          this.items = [];
          this.bookTitles = new Map();
          this.loading = false;
        }
        this.loadHistory();
      },
      error: (err) => {
        this.undoError = err.error ?? 'Ongedaan maken mislukt.';
        this.cdr.detectChanges();
      },
    });
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

  generateExcel() {
    const payload: SchoolItemCopyExcelPayload = {
      rows: this.selectedItems.map((item) => ({
        itemName: this.bookTitles.get(item.copy.itemId) || '',
        copyId: item.copy.barcode,
      })),
    };

    this.api.downloadImportExcelSheet(payload).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `barcodes_${new Date().getTime()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
    });
  }

  ngOnDestroy(): void {
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
  }
}
