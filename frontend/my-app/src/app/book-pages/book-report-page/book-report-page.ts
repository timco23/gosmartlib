import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Book, BookStatisticsDTO, CampusStatisticsDTO, Readinglevel, RecentLendingDTO } from '../../../types/types';
import { BookService } from '../../services/book-service';
import { UserService } from '../../services/user.service';
import { StatisticsService } from '../../services/statistics.service';
import { ReadingLevelService } from '../../services/reading-level.service';

@Component({
  selector: 'app-book-report-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './book-report-page.html',
  styleUrl: './book-report-page.css',
})
export class BookReportPage implements OnInit {
  route = inject(ActivatedRoute);
  bookApi = inject(BookService);
  statisticsApi = inject(StatisticsService);
  userApi = inject(UserService);
  readingLevelService = inject(ReadingLevelService);
  cdr = inject(ChangeDetectorRef);

  bookId!: number;
  book: Book | null = null;
  stats: BookStatisticsDTO | null = null;
  readingLevels: Readinglevel[] = [];
  loading = true;
  chartMode: 'month' | 'year' = 'month';
  trendLimit = 9;
  private schoolId!: number;

  ngOnInit(): void {
    this.bookId = Number(this.route.snapshot.paramMap.get('id'));

    this.bookApi.getBookById(this.bookId).subscribe((book) => {
      this.book = book;
      this.cdr.detectChanges();
    });

    this.readingLevelService.getAll().subscribe((levels) => {
      this.readingLevels = levels ?? [];
    });

    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
        this.loadStats();
      }
    });
  }

  setChartMode(mode: 'month' | 'year'): void {
    if (this.chartMode === mode) return;
    this.chartMode = mode;
    this.loadStats();
  }

  private loadStats(): void {
    this.statisticsApi
      .getBookStatistics(this.bookId, this.schoolId, this.chartMode, this.chartMode === 'year' ? 0 : this.trendLimit)
      .subscribe((stats) => {
        this.stats = stats;
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  get maxChartCount(): number {
    return Math.max(...(this.stats?.lendingsByMonth.map((m) => m.count) ?? [1]), 1);
  }

  get totalCopies(): number {
    return this.stats?.campusStats.reduce((sum, s) => sum + s.totalCopies, 0) ?? 0;
  }

  /** Name of the book's reading level, looked up from the shared reading-levels list. */
  get rankingReadingLevelName(): string {
    return this.readingLevels.find((l) => l.id === this.book?.readingLevel)?.name ?? '';
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

  formatChartLabel(key: string): string {
    if (!key.includes('-')) return key;
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

  /** Bar width (0–100%): 100% = rank 1, 0% = last place. */
  rankingBar(rank: number, total: number): number {
    if (total <= 1) return 100;
    return Math.round(((total - rank) / (total - 1)) * 100);
  }

  /** Percentage of books this ranking beats (0–100). */
  betterThanPercent(rank: number, total: number): number {
    return Math.round(((total - rank) / total) * 100);
  }

  /** True when the ranking is in the top third — used to apply green styling. */
  rankingIsGood(rank: number, total: number): boolean {
    return this.rankingBar(rank, total) >= 66;
  }

  get rankingBarAbsolute(): number {
    const r = this.stats?.schoolRanking;
    return r ? this.rankingBar(r.rankAbsolute, r.totalBooks) : 0;
  }

  get rankingBarPerCopy(): number {
    const r = this.stats?.schoolRanking;
    return r ? this.rankingBar(r.rankPerCopy, r.totalBooks) : 0;
  }

  get rankingBarReadingLevel(): number {
    const r = this.stats?.schoolRanking;
    if (!r?.rankReadingLevel || !r.totalBooksReadingLevel) return 0;
    return this.rankingBar(r.rankReadingLevel, r.totalBooksReadingLevel);
  }
}
