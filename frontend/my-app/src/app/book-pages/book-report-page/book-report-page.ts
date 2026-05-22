import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Book, BookStatisticsDTO, CampusStatisticsDTO, Readinglevel } from '../../../types/types';
import { BookService } from '../../services/book-service';
import { UserService } from '../../services/user.service';
import { StatisticsService } from '../../services/statistics.service';
import { ReadingLevelService } from '../../services/reading-level.service';
import { formatChartLabel, formatDate, lendingStatus, lendingStatusLabel } from '../../shared/statistics-utils';

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

  readonly lendingStatus = lendingStatus;
  readonly lendingStatusLabel = lendingStatusLabel;
  readonly formatDate = formatDate;
  readonly formatChartLabel = formatChartLabel;

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

  get rankingReadingLevelName(): string {
    return this.readingLevels.find((l) => l.id === this.book?.readingLevel)?.name ?? '';
  }

  campusBadge(s: CampusStatisticsDTO): 'onbenut' | null {
    return s.lendingCount === 0 ? 'onbenut' : null;
  }

  campusBadgeLabel(badge: ReturnType<BookReportPage['campusBadge']>): string {
    return badge === 'onbenut' ? 'Niet gebruikt' : '';
  }

  rankingBar(rank: number, total: number): number {
    if (total <= 1) return 100;
    return Math.round(((total - rank) / (total - 1)) * 100);
  }

  betterThanPercent(rank: number, total: number): number {
    return Math.round(((total - rank) / total) * 100);
  }

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
