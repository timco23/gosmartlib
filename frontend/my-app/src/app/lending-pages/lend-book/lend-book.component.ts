import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book-service';
import { Book, Lending, School, SchoolItemCopy, UserProfile } from '../../../types/types';
import { SchoolService } from '../../services/school.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { LendingService } from '../../services/lending.service';

@Component({
  selector: 'app-lend-book',
  imports: [RouterLink, FormsModule],
  templateUrl: './lend-book.component.html',
  styleUrl: './lend-book.component.css'
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
      this.barcode = '';
      this.cdr.detectChanges();
      return;
    }
    this.bookSelected = true;

    const selected = this.books.find(b => b.id === +id);
    this.currentBook = selected ?? null;
    this.currentSchoolCopy = null;
    this.currentCopies = [];

    if (selected && this.school && selected.id !== undefined && this.school.id !== undefined) {
      this.bookService.getCopiesByBookId(selected.id, this.school.id).subscribe({
        next: (copies) => {
          this.currentCopies = copies;
          this.cdr.detectChanges();
        },
        error: () => {
          this.currentCopies = [];
          this.cdr.detectChanges();
        }
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

    this.currentSchoolCopy = this.currentCopies.find(c => c.barcode === barcode) ?? null;
    this.cdr.detectChanges();
  }

  onUserSelect(event: Event): void {
    const userId = (event.target as HTMLSelectElement).value;
    if (!userId) {
      this.currentUser = null;
      this.cdr.detectChanges();
      return;
    }
    this.currentUser = this.users.find(u => u.id === +userId) ?? null;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    const request = {
      borrowerId: this.currentUser?.id ?? -1,
      copyBarcode: this.currentSchoolCopy?.barcode ?? '',
      schoolId: this.school?.id ?? -1,
      lendingDate: new Date().toISOString().split('T')[0],
      returnDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    };

    this.lendingService.createLending(request).subscribe({
      next: () => {
        alert('Lending created successfully!');
        this.router.navigate(['/lendings']);
      },
      error: (err) => {
        console.error('Error creating lending:', err);
        alert('Failed to create lending. Please try again.');
      }
    });
  }
}