import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Book, School, SchoolItemCopy, UserProfile } from '../../../types/types';
import { SchoolService } from '../../services/school.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { BookService } from '../../services/book-service';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LendingService } from '../../services/lending.service';

@Component({
  selector: 'app-return-book',
  imports: [FormsModule, RouterLink],
  templateUrl: './return-book.component.html',
  styleUrl: './return-book.component.css'
})
export class ReturnBookComponent {
  public currentSchoolCopy: SchoolItemCopy | null = null;
  public currentCopies: SchoolItemCopy[] = [];
  public currentBook: Book | null = null;
  school: School | null = null;
  public books: Book[] = [];
  public users: UserProfile[] = [];
  public lendings: any[] = [];
  public currentLending: any = null;

  public bookSelected: boolean = false;
  public barcode: string = '';

  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  private schoolService = inject(SchoolService);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private lendingService = inject(LendingService);
  private route = inject(ActivatedRoute);

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

                this.userService.getFromSchool(this.school?.id ?? -1).subscribe({
                  next: (users) => {
                    this.users = users;
                    this.cdr.detectChanges();
                  }
                });
              }
            });
          }
        }
      });
    });

    this.lendingService.getLendings().subscribe({
      next: (lendings) => {
        this.lendings = lendings;
        this.cdr.detectChanges();
      },
      error: () => {
        this.lendings = [];
        this.cdr.detectChanges();
      }
    });

    this.bookService.getBooks().subscribe({
      next: (books) => {
        this.books = books;
        this.cdr.detectChanges();
      },
      error: () => {
        this.books = [];
        this.cdr.detectChanges();
      }
    });

    this.route.queryParams.subscribe(params => {
      if (params['barcode']) {
        this.barcode = params['barcode'];
        this.onBarcodeChange(this.barcode);
      }
    });
  }

  onBarcodeChange(barcode: string): void {
    if (!barcode.trim()) {
      this.currentBook = null;
      this.currentSchoolCopy = null;
      this.currentLending = null;
      this.cdr.detectChanges();
      return;
    }

    this.currentLending = this.lendings.find(l => l.copyBarcode === barcode) ?? null;

    if (!this.currentLending) {
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
          }
        });
      },
      error: () => {
        this.currentSchoolCopy = null;
        this.currentBook = null;
        this.cdr.detectChanges();
      }
    });
  }

  onBookSelect(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    if (!id) {
      this.bookSelected = false;
      this.currentBook = null;
      this.currentSchoolCopy = null;
      this.currentCopies = [];
      this.currentLending = null;
      this.barcode = '';
      this.cdr.detectChanges();
      return;
    }

    this.bookSelected = true;
    const selected = this.books.find(b => b.id === +id);
    this.currentBook = selected ?? null;
    this.currentLending = this.lendings.find(l => l.bookTitle === selected?.title) ?? null;

    if (this.currentLending?.copyBarcode) {
      this.barcode = this.currentLending.copyBarcode;
      this.onBarcodeChange(this.barcode);
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

    this.currentSchoolCopy = this.currentCopies.find(c => c.barcode === barcode) ?? null;
    this.cdr.detectChanges();
  }

  onUserSelect(event: Event): void {
    const userId = (event.target as HTMLSelectElement).value;
    this.cdr.detectChanges();
  }
}