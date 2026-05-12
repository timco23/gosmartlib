import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Book, BookStatisticsDTO, CampusStatisticsDTO, RecentLendingDTO } from '../../../types/types';
import { BookService } from '../../services/book-service';
import { LendingService } from '../../services/lending.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-book-report-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './book-report-page.html',
  styleUrl: './book-report-page.css',
})
export class BookReportPage implements OnInit {
  route = inject(ActivatedRoute);
  bookApi = inject(BookService);
  lendingApi = inject(LendingService);
  userApi = inject(UserService);
  cdr = inject(ChangeDetectorRef);

  bookId!: number;
  book: Book | null = null;
  stats: BookStatisticsDTO | null = null;
  loading = true;

  ngOnInit(): void {
    this.bookId = Number(this.route.snapshot.paramMap.get('id'));

    this.bookApi.getBookById(this.bookId).subscribe((book) => {
      this.book = book;
      this.cdr.detectChanges();
    });

    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.lendingApi.getBookStatistics(this.bookId, user.schoolId).subscribe((stats) => {
          this.stats = stats;
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  get maxMonthCount(): number {
    return Math.max(...(this.stats?.lendingsByMonth.map((m) => m.count) ?? [1]), 1);
  }

  get totalCopies(): number {
    return this.stats?.campusStats.reduce((sum, s) => sum + s.totalCopies, 0) ?? 0;
  }

  campusBadge(s: CampusStatisticsDTO): 'onbenut' | null {
    return s.lendingCount === 0 ? 'onbenut' : null;
  }

  campusBadgeLabel(badge: ReturnType<BookReportPage['campusBadge']>): string {
    return badge === 'onbenut' ? 'Niet gebruikt' : '';
  }

  lendingStatus(l: RecentLendingDTO): 'returned' | 'overdue' | 'active' {
    if (l.returned) return 'returned';
    if (new Date() > new Date(l.returnDate)) return 'overdue';
    return 'active';
  }

  lendingStatusLabel(l: RecentLendingDTO): string {
    const s = this.lendingStatus(l);
    if (s === 'returned') return 'Teruggebracht';
    if (s === 'overdue') return 'Te laat';
    return 'Actief';
  }

  formatMonth(key: string): string {
    const [year, month] = key.split('-');
    return new Date(Number(year), Number(month) - 1).toLocaleDateString('nl-BE', {
      month: 'short',
      year: '2-digit',
    });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('nl-BE', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  }
}
