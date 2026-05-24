import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { filter, take } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { LendingService } from '../services/lending.service';
import { Lending, LoginResponse, Role, LendingStatus, Book, School } from '../../types/types';
import { UserService } from '../services/user.service';
import { SchoolService } from '../services/school.service';
import { BookService } from '../services/book-service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard-component',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
})
export class DashboardComponent implements OnInit {
  lendings: Lending[] = [];
  loading = true;
  error: string | null = null;
  currentUser: LoginResponse | null = null;
  readonly dueSoonDays = 3;

  mostBorrowedBook: Book | null = null;
  lastBorrowedBook: Book | null = null;
  school: School | null = null;

  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  constructor(
    private lendingApi: LendingService,
    public authService: AuthService,
    private userService: UserService,
    private schoolService: SchoolService,
    private bookService: BookService,
  ) {}

  ngOnInit() {
    this.authService.currentUser$
      .pipe(
        filter((u): u is LoginResponse => !!u),
        take(1),
      )
      .subscribe((user) => {
        this.currentUser = user;
        this.loadLendings(user.email);

        this.userService.getUser(user.email).subscribe((userData) => {
          if (!userData.schoolName) return;
          this.schoolService.getByName(userData.schoolName).subscribe((school) => {
            this.school = school;

            if (school.dashboardShowBookOfMonth) {
              this.lendingApi.getMostBorrowedThisMonth(school.id!).subscribe({
                next: (book) => {
                  this.mostBorrowedBook = book;
                  this.cdr.detectChanges();
                },
              });
            }

            if (school.dashboardShowLastBorrowed) {
              this.lendingApi.getLastBorrowedBook(user.email).subscribe({
                next: (book) => {
                  this.lastBorrowedBook = book;
                  this.cdr.detectChanges();
                },
              });
            }
          });
        });
      });
  }

  private loadLendings(email: string) {
    this.loading = true;
    this.error = null;

    this.lendingApi.getLendingsByUser(email).subscribe({
      next: (data) => {
        this.lendings = data ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Uitleningen konden niet geladen worden.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  trackById = (_: number, lending: Lending) => lending.id;

  showManageActions(): boolean {
    return this.authService.atLeastHasRole(Role.BIBLIOTHEEKBEHEERDER);
  }

  get filteredAndSortedLendings(): Lending[] {
    return [...this.lendings.filter((l) => !l.returned)].sort((a, b) =>
      this.compareLendings(a, b),
    );
  }

  private compareLendings(a: Lending, b: Lending): number {
    const rankDiff = this.getStatusRank(a) - this.getStatusRank(b);
    if (rankDiff !== 0) return rankDiff;

    const aDate = this.getSortDate(a);
    const bDate = this.getSortDate(b);
    if (aDate !== bDate) return aDate - bDate;

    return (a.bookTitle ?? '').localeCompare(b.bookTitle ?? '');
  }

  private getStatusRank(lending: Lending): number {
    switch (this.getStatus(lending)) {
      case 'OVERDUE':
        return 0;
      case 'DUE_SOON':
        return 1;
      case 'OK':
        return 2;
      case 'RETURNED':
        return 3;
      default:
        return 4;
    }
  }

  private getStatus(lending: Lending): LendingStatus {
    if (lending.returned) return 'RETURNED';
    const days = this.daysLeft(lending);
    if (days === null) return 'OK';
    if (days < 0) return 'OVERDUE';
    if (days <= this.dueSoonDays) return 'DUE_SOON';
    return 'OK';
  }

  private getSortDate(lending: Lending): number {
    const dateValue = lending.returned ? lending.dateReturned : lending.returnDate;
    const date = this.toDate(dateValue ?? null);
    return date ? date.getTime() : Number.MAX_SAFE_INTEGER;
  }

  tileClass(lending: Lending): string {
    const status = this.getStatus(lending);
    if (status === 'RETURNED') return 'returned';
    if (status === 'OVERDUE') return 'overdue';
    if (status === 'DUE_SOON') return 'due-soon';
    return 'ok';
  }

  statusLabel(lending: Lending): string {
    const status = this.getStatus(lending);
    if (status === 'RETURNED') return 'Teruggebracht';
    if (status === 'OVERDUE') return 'Te laat';
    if (status === 'DUE_SOON') return 'Bijna verlopen';
    return 'Op tijd';
  }

  daysLeftLabel(lending: Lending): string {
    const days = this.daysLeft(lending);
    if (days === null) return '-';
    if (days < 0) return `${Math.abs(days)} dagen te laat`;
    return `${days} dagen`;
  }

  daysLeft(lending: Lending): number | null {
    if (!lending.returnDate || lending.returned) return null;
    const returnDate = this.toDate(lending.returnDate);
    if (!returnDate) return null;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const diffMs = returnDate.getTime() - today.getTime();
    return Math.ceil(diffMs / 86400000);
  }

  formatDate(value: string | null | undefined): string {
    const date = this.toDate(value ?? null);
    return date ? date.toLocaleDateString('nl-BE') : '-';
  }

  extend(lending: Lending) {
    this.lendingApi.extendLending(lending.id).subscribe({
      next: (updated) => this.replace(updated),
      error: () => (this.error = 'Verlengen mislukt.'),
    });
  }

  returnBook(lending: Lending) {
    const payload: Lending = {
      ...lending,
      returned: true,
      dateReturned: this.todayString(),
    };

    this.lendingApi.updateLending(lending.id, payload).subscribe({
      next: (updated) => this.replace(updated),
      error: () => (this.error = 'Terugbrengen mislukt.'),
    });
  }

  private replace(updated: Lending) {
    this.lendings = this.lendings.map((l) => (l.id === updated.id ? updated : l));
  }

  private todayString(): string {
    const d = new Date();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
  }

  private toDate(value: string | null): Date | null {
    if (!value) return null;
    const datePart = value.length >= 10 ? value.substring(0, 10) : value;
    const date = new Date(`${datePart}T00:00:00`);
    return isNaN(date.getTime()) ? null : date;
  }

  coverUrl(itemId: number): string {
    return this.bookService.getBookCoverUrl(itemId);
  }
}
