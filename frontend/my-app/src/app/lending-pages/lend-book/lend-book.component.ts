import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book-service';
import { Book, Lending, School, SchoolItemCopy, UserProfile } from '../../../types/types';
import { SchoolService } from '../../services/school.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { LendingService } from '../../services/lending.service';
import { BookSelectorPopupComponent } from '../../book-pages/book-selector-popup-component/book-selector-popup-component';

@Component({
  selector: 'app-lend-book',
  imports: [RouterLink, FormsModule, BookSelectorPopupComponent],
  templateUrl: './lend-book.component.html',
  styleUrl: './lend-book.component.css',
})
export class LendBookComponent {
  public currentSchoolCopy: SchoolItemCopy | null = null;
  public currentCopies: SchoolItemCopy[] = [];
  public currentBook: Book | null = null;
  public users: UserProfile[] = [];
  school: School | null = null;
  public books: Book[] = [];
  public currentLending: Lending | null = null;
  public currentUser: UserProfile | null = null;

  public bookSelected: boolean = false;
  public barcode: string = '';

  isBookPopupOpen = false;
  selectedBookId: string | number | null = null;
  selectedBook: any | null = null;

  bookApi = inject(BookService);

  readonly bookId = (b: any) => b?.id ?? b?.bookId;
  readonly bookTitle = (b: any) => b?.title ?? b?.bookTitle ?? 'Onbekend boek';
  readonly bookMeta = (b: any) => [b?.author ?? b?.authorName, b?.isbn].filter(Boolean).join(' • ');

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

  constructor(private bookService: BookService) {}

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

        this.currentSchoolCopy = copy;

        this.bookService.getBookById(copy.itemId).subscribe({
          next: (book) => {
            this.currentBook = book ?? null;
            this.cdr.detectChanges();
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

  onCopySelect(event: Event): void {
    const barcode = (event.target as HTMLSelectElement).value;
    if (!barcode) {
      this.currentSchoolCopy = null;
      this.cdr.detectChanges();
      return;
    }

    this.currentSchoolCopy = this.currentCopies.find((c) => c.barcode === barcode) ?? null;
    this.cdr.detectChanges();
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
    if (!this.currentUser || !this.currentSchoolCopy || !this.school) return;

    const request = {
      borrowerId: this.currentUser.id ?? -1,
      copyBarcode: this.currentSchoolCopy.barcode ?? '',
      schoolId: this.school.id ?? -1,
      lendingDate: new Date().toISOString().split('T')[0],
      returnDate: new Date(Date.now() + this.school.lendingPeriodDays * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0],
    };

    console.log('user: ', this.currentUser);
    console.log('aantal toegelaten leningen: ', this.school.maxLendsStudent);
    console.log('aantal leningen gebruiker: ', this.currentUser.items_borrowed);

    if (this.currentUser.items_borrowed < this.school.maxLendsStudent) {
      this.lendingService.createLending(request).subscribe({
        next: () => {
          this.bookService.setCopyStatus(this.currentSchoolCopy!.id, 'UITGELEEND').subscribe({
            next: () => {
              console.log('Copy status updated');
            },
            error: (err) => {
              console.error('Error updating copy status:', err);
              alert('Lening aanmaken mislukt, probeer later opnieuw.');
            },
          });

          this.userService.addBorrowedItem(this.currentUser!.id ?? -1).subscribe({
            next: () => {
              console.log('User borrowed item count updated');
              this.router.navigate(['/lendings']);
            },
            error: (err) => {
              console.error('Error creating lending:', err);
              alert('Lening aanmaken mislukt, probeer later opnieuw.');
            },
          });
        },
      });
    } else {
      alert('Deze gebruiker leent al het maximum aantal boeken uit.');
    }
  }

  coverSrcFromLending(lending: any): string {
    const book = lending?.book ?? lending?.item ?? lending ?? this.currentBook ?? null;
    return this.bookApi.getBookCoverSrc(book) ?? '';
  }
}
