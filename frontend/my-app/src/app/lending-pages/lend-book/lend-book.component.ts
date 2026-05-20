import { ChangeDetectorRef, Component, ElementRef, inject, ViewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book-service';
import { Book, Lending, School, SchoolItemCopy, UserProfile } from '../../../types/types';
import { SchoolService } from '../../services/school.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { LendingService } from '../../services/lending.service';
import { BookSelectorPopupComponent } from '../../book-pages/book-selector-popup-component/book-selector-popup-component';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import { UserSelectorPopupComponent } from '../../user-selector-popup/user-selector-popup.component';

@Component({
  selector: 'app-lend-book',
  imports: [RouterLink, FormsModule, BookSelectorPopupComponent, PopUpComponent, UserSelectorPopupComponent],
  templateUrl: './lend-book.component.html',
  styleUrl: './lend-book.component.css',
})
export class LendBookComponent {
  public currentSchoolCopy: SchoolItemCopy | null = null;
  public selectedCopies: SchoolItemCopy[] = [];
  public currentCopies: SchoolItemCopy[] = [];
  public currentBook: Book | null = null;
  public users: UserProfile[] = [];
  school: School | null = null;
  public books: Book[] = [];
  public currentLending: Lending | null = null;
  public currentUser: UserProfile | null = null;
  public duration: number = 0;
  canAddInBulk: boolean = false;
  public pendingBooks: { book: Book; copies: SchoolItemCopy[]; selectedCopies: SchoolItemCopy[] }[] = [];
  public showCopyPicker: boolean = false;

  private _showDurationInput: boolean = false;
  showMultipleBooks: boolean = false;
  public selectedBooks: { book: Book; copy: SchoolItemCopy }[] = [];

  showAlreadyAdded: boolean = false;
  showBookUnavailable: boolean = false;
  showMaxLendings: boolean = false;

  @ViewChild('barcodeInput') barcodeInputRef!: ElementRef<HTMLInputElement>;
  
  get showDurationInput(): boolean {
    return this._showDurationInput;
  }

  get totalPendingSelected(): number {
    return this.pendingBooks.reduce((acc, e) => acc + e.selectedCopies.length, 0);
  }
  
  set showDurationInput(value: boolean) {
    this._showDurationInput = value;
    if (!value) {
      this.duration = this.school?.lendingPeriodDays ?? 0;
    }
  }

  public bookSelected: boolean = false;
  public barcode: string = '';

  isBookPopupOpen = false;
  selectedBookId: string | number | null = null;
  selectedBook: any | null = null;

  bookApi = inject(BookService);

  readonly bookId = (b: any) => b?.id ?? b?.bookId;
  readonly bookTitle = (b: any) => b?.title ?? b?.bookTitle ?? 'Onbekend boek';
  readonly bookMeta = (b: any) => [b?.author ?? b?.authorName, b?.isbn].filter(Boolean).join(' • ');

  isUserPopupOpen = false;

  openUserPopup(): void { this.isUserPopupOpen = true; }
  closeUserPopup(): void { this.isUserPopupOpen = false; }

  onUserSelected(user: UserProfile): void {
    this.currentUser = user;
    this.canAddInBulk = user.role !== 'LEERLING';
    this.isUserPopupOpen = false;
    this.cdr.detectChanges();
  }

  openBookPopup(): void {
    this.isBookPopupOpen = true;
  }
  closeBookPopup(): void {
    this.isBookPopupOpen = false;
  }

  onBookSelected(payload: any): void {
    const selected = payload?.book ?? payload;
    const id = Number(this.bookId(selected));

    if (!id || !this.school?.id) {
      this.clearSelectedBook();
      return;
    }

    this.selectedBook = selected;
    this.selectedBookId = id;
    this.bookSelected = true;


    this.currentBook = selected;
    this.currentSchoolCopy = null;
    this.currentCopies = [];
    this.barcode = '';


    this.bookService.getCopiesByBookId(id, this.school.id).subscribe({
      next: (copies) => {
        this.currentCopies = copies.filter((c) => c.copy_status === 'BESCHIKBAAR');
        this.cdr.detectChanges();
      },
      error: () => {
        this.currentCopies = [];
        this.cdr.detectChanges();
      },
    });

    this.closeBookPopup();
  }

  clearSelectedBook(): void {
    this.selectedBook = null;
    this.selectedBookId = null;
    this.bookSelected = false;
    this.currentBook = null;
    this.currentSchoolCopy = null;
    this.currentCopies = [];
    this.barcode = '';
    this.cdr.detectChanges();
  }

  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  private schoolService = inject(SchoolService);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private lendingService = inject(LendingService);
  private router = inject(Router);

  constructor(private bookService: BookService) { }

  public ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;
      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (data.schoolName != null) {
            this.schoolService.getByName(data.schoolName).subscribe({
              next: (schoolData) => {
                this.school = schoolData;
                const schoolId = this.school!.id!;

                this.userService.getFromSchool(schoolId).subscribe({
                  next: (users) => {
                    this.users = users;
                    this.duration = this.school?.lendingPeriodDays ?? 0;
                    this.cdr.detectChanges();
                  },
                });

                this.bookService.getBooks().subscribe({
                  next: (books) => {
                    const bookChecks = books.map((book) =>
                      this.bookService
                        .getCopiesByBookId(book.id, schoolId)
                        .toPromise()
                        .then((copies) => {
                          return {
                            book,
                            hasAvailable:
                              copies?.some((c) => c.copy_status === 'BESCHIKBAAR') ?? false,
                          };
                        }),
                    );

                    Promise.all(bookChecks).then((results) => {
                      this.books = results.filter((r) => r.hasAvailable).map((r) => r.book);
                      this.cdr.detectChanges();
                    });
                  },
                });
              },
            });
          }
        },
      });
    });
  }

  onBarcodeChange(barcode: string): void {
    if (!barcode.trim()) {
      this.currentBook = null;
      this.currentSchoolCopy = null;
      this.cdr.detectChanges();
      return;
    }

    this.bookService.getByBarcode(barcode).subscribe({
      next: (copy) => {
        if (!copy) {
          this.currentSchoolCopy = null;
          this.currentBook = null;
          this.cdr.detectChanges();
          return;
        }

        if (copy.copy_status !== 'BESCHIKBAAR') {
          this.showBookUnavailable = true;
          this.barcode = '';
          this.currentSchoolCopy = null;
          this.currentBook = null;
          this.cdr.detectChanges();
          setTimeout(() => this.barcodeInputRef?.nativeElement?.focus(), 0);
          return;
        }

        this.currentSchoolCopy = copy;

        this.bookService.getBookById(copy.itemId).subscribe({
          next: (book) => {
            this.currentBook = book ?? null;
            this.cdr.detectChanges();

            const isMultiUser = this.currentUser?.role !== 'LEERLING' && this.currentUser !== null;

            if (isMultiUser && this.currentBook && this.currentSchoolCopy) {
              const alreadyAdded = this.selectedBooks.some(
                (s) => s.copy.barcode === this.currentSchoolCopy!.barcode
              );
              if (alreadyAdded) {
                this.showAlreadyAdded = true;
                this.barcode = '';
                this.currentBook = null;
                this.currentSchoolCopy = null;
                this.cdr.detectChanges();
                setTimeout(() => this.barcodeInputRef?.nativeElement?.focus(), 0);
                return;
              }

              this.selectedBooks.push({ book: this.currentBook, copy: this.currentSchoolCopy });
              this.selectedBooks.sort((a, b) => a.book.title.localeCompare(b.book.title));
              this.showMultipleBooks = true;
              this.currentBook = null;
              this.currentSchoolCopy = null;
              this.barcode = '';
              this.cdr.detectChanges();
              setTimeout(() => this.barcodeInputRef?.nativeElement?.focus(), 0);
            }
          },
          error: () => {
            this.currentBook = null;
            this.cdr.detectChanges();
          },
        });
      },
      error: () => {
        this.currentSchoolCopy = null;
        this.currentBook = null;
        this.cdr.detectChanges();
      },
    });
  }

  onBookSelect(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    if (!id) {
      this.bookSelected = false;
      this.currentBook = null;
      this.currentSchoolCopy = null;
      this.currentCopies = [];
      this.barcode = '';
      this.cdr.detectChanges();
      return;
    }
    this.bookSelected = true;

    const selected = this.books.find((b) => b.id === +id);
    this.currentBook = selected ?? null;
    this.currentSchoolCopy = null;
    this.currentCopies = [];

    if (selected && this.school && selected.id !== undefined && this.school.id !== undefined) {
      this.bookService.getCopiesByBookId(selected.id, this.school.id).subscribe({
        next: (copies) => {
          this.currentCopies = copies.filter((c) => c.copy_status === 'BESCHIKBAAR');
          this.cdr.detectChanges();
        },
        error: () => {
          this.currentCopies = [];
          this.cdr.detectChanges();
        },
      });
    }
    this.cdr.detectChanges();
  }

  onBooksConfirmed(books: any[]): void {
    if (!this.school?.id) return;

    const checks = books.map(book =>
      this.bookService.getCopiesByBookId(Number(this.bookId(book)), this.school!.id!)
        .toPromise()
        .then(copies => {
          const available = copies?.filter(c => c.copy_status === 'BESCHIKBAAR') ?? [];
          return { book, copies: available, selectedCopies: [] as SchoolItemCopy[] };
        })
    );

    Promise.all(checks).then(results => {
      this.pendingBooks = results.filter(r => r.copies.length > 0);
      this.showCopyPicker = true;
      this.closeBookPopup();
      this.cdr.detectChanges();
    });
  }

  togglePendingCopy(entry: { selectedCopies: SchoolItemCopy[] }, copy: SchoolItemCopy): void {
    const idx = entry.selectedCopies.findIndex(c => c.barcode === copy.barcode);
    if (idx > -1) {
      entry.selectedCopies.splice(idx, 1);
    } else {
      entry.selectedCopies.push(copy);
    }
    this.cdr.detectChanges();
  }

  isPendingCopySelected(entry: { selectedCopies: SchoolItemCopy[] }, copy: SchoolItemCopy): boolean {
    return entry.selectedCopies.some(c => c.barcode === copy.barcode);
  }

  confirmPendingBooks(): void {
    for (const entry of this.pendingBooks) {
      for (const copy of entry.selectedCopies) {
        const alreadyAdded = this.selectedBooks.some(s => s.copy.barcode === copy.barcode);
        if (!alreadyAdded) {
          this.selectedBooks.push({ book: entry.book, copy });
        }
      }
    }
    this.selectedBooks.sort((a, b) => a.book.title.localeCompare(b.book.title));
    this.showMultipleBooks = true;
    this.pendingBooks = [];
    this.showCopyPicker = false;
    this.clearSelectedBook();
    this.cdr.detectChanges();
  }

  cancelPendingBooks(): void {
    this.pendingBooks = [];
    this.showCopyPicker = false;
    this.cdr.detectChanges();
  }

  onCopyToggle(copy: SchoolItemCopy, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (this.showMultipleBooks) {
      if (checked) {
        this.selectedCopies = [...this.selectedCopies, copy];
      } else {
        this.selectedCopies = this.selectedCopies.filter(c => c.barcode !== copy.barcode);
      }
      this.currentSchoolCopy = this.selectedCopies[0] ?? null;
    } else {
      this.selectedCopies = [copy];
      this.currentSchoolCopy = copy;
    }

    this.cdr.detectChanges();
  }

  isCopySelected(copy: SchoolItemCopy): boolean {
    return this.selectedCopies.some(c => c.barcode === copy.barcode);
  }

  onUserSelect(event: Event): void {
    const userId = (event.target as HTMLSelectElement).value;
    if (!userId) {
      this.currentUser = null;
      this.cdr.detectChanges();
      return;
    }
    this.currentUser = this.users.find((u) => u.id === +userId) ?? null;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    let copies: SchoolItemCopy[] = [];

    if (this.showMultipleBooks) {
      copies = this.selectedBooks.map((s) => s.copy);
    } else if (this.currentSchoolCopy) {
      copies = [this.currentSchoolCopy];
    }

    if (!this.currentUser || copies.length === 0 || !this.school) {
      return;
    }

    const remainingCapacity = this.school.maxLendsStudent - this.currentUser.items_borrowed;
    if (copies.length > remainingCapacity) {
      this.showMaxLendings = true;
      return;
    }

    const requests = copies.map((copy) => ({
      borrowerId: this.currentUser!.id ?? -1,
      copyBarcode: copy.barcode ?? '',
      schoolId: this.school!.id ?? -1,
      lendingDate: new Date().toISOString().split('T')[0],
      returnDate: new Date(Date.now() + this.duration * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0],
    }));

    const submits = requests.map((request, i) =>
      this.lendingService
        .createLending(request)
        .toPromise()
        .then(() => this.bookService.setCopyStatus(copies[i].id, 'UITGELEEND').toPromise())
    );

    Promise.all(submits)
      .then(() => {
        const increments = copies.map(() =>
          this.userService.addBorrowedItem(this.currentUser!.id ?? -1).toPromise()
        );
        return Promise.all(increments);
      })
      .then(() => this.router.navigate(['/lendings']))
      .catch((err) => {
        console.error('Error creating lendings:', err);
        alert(err.message + this.barcode || 'Er is een fout opgetreden bij het aanmaken van de leningen.');
      });
  }

  coverSrcFromLending(lending: any): string {
    const book = lending?.book ?? lending?.item ?? lending ?? this.currentBook ?? null;
    return this.bookApi.getBookCoverSrc(book) ?? '';
  }

  addBookToSelection(): void {
    if (!this.currentBook || this.selectedCopies.length === 0) {
      return;
    }
  
    for (const copy of this.selectedCopies) {
      let alreadyAdded = false;
      for (const s of this.selectedBooks) {
        if (s.copy.barcode === copy.barcode) {
          alreadyAdded = true;
          break;
        }
      }
      if (!alreadyAdded) {
        this.selectedBooks.push({ book: this.currentBook, copy: copy });
      }
    }
  
    this.clearSelectedBook();
    this.selectedCopies = [];
    this.cdr.detectChanges();
  }

  removeBookFromSelection(index: number): void {
    this.selectedBooks.splice(index, 1);
    this.cdr.detectChanges();
  }
  closeNotification(type: 'added' | 'unavailable' | 'maxLendings'): void {
    if (type === 'added') {
      this.showAlreadyAdded = false;
    } else if (type === 'unavailable') {
      this.showBookUnavailable = false;
    }
    else if (type === 'maxLendings') {
      this.showMaxLendings = false;
    }
  }

  getClasses(user: UserProfile): string {
    return user.classes.map(clazz => clazz.className).join(" ")
  }

  get noPendingCopiesSelected(): boolean {
    return this.pendingBooks.every(e => e.selectedCopies.length === 0);
  }
}
