import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Book, Lending, SchoolItemCopy } from '../../../types/types';
import { BookService } from '../../services/book-service';
import { LendingService } from '../../services/lending.service';
import { UserService } from '../../services/user.service';

interface CampusStats {
  name: string;
  totalCopies: number;
  availableCopies: number;
  lendingCount: number;
  activeLendings: number;
  avgDuration: number;
  lendingsPerCopy: number;
  unavailableDays: number;
}

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
  lendings: Lending[] = [];
  copies: SchoolItemCopy[] = [];
  loading = true;

  ngOnInit(): void {
    this.bookId = Number(this.route.snapshot.paramMap.get('id'));

    this.bookApi.getBookById(this.bookId).subscribe((book) => {
      this.book = book;
      this.cdr.detectChanges();
    });

    this.lendingApi.getLendingsByBookId(this.bookId).subscribe((data) => {
      this.lendings = data ?? [];
      this.loading = false;
      this.cdr.detectChanges();
    });

    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.bookApi.getCopiesByBookId(this.bookId, user.schoolId).subscribe((copies) => {
          this.copies = copies;
          this.cdr.detectChanges();
        });
      }
    });
  }

  // ── Globale stats ──────────────────────────────────────────────────────

  get totalLendings(): number {
    return this.lendings.length;
  }

  get activeLendings(): number {
    return this.lendings.filter((l) => !l.returned).length;
  }

  get avgDurationDays(): number {
    const returned = this.lendings.filter((l) => l.returned && l.dateReturned);
    if (!returned.length) return 0;
    const sum = returned.reduce((acc, l) => {
      return acc + (new Date(l.dateReturned!).getTime() - new Date(l.lendingDate).getTime()) / 86_400_000;
    }, 0);
    return Math.round(sum / returned.length);
  }

  get lateReturnPercent(): number {
    if (!this.totalLendings) return 0;
    const today = new Date();
    const late = this.lendings.filter((l) => {
      const due = new Date(l.returnDate);
      return l.dateReturned ? new Date(l.dateReturned) > due : !l.returned && today > due;
    }).length;
    return Math.round((late / this.totalLendings) * 100);
  }

  get extensionPercent(): number {
    if (!this.totalLendings) return 0;
    return Math.round((this.lendings.filter((l) => l.extended).length / this.totalLendings) * 100);
  }

  get lendingsPerCopy(): number {
    if (!this.copies.length) return 0;
    return Math.round((this.totalLendings / this.copies.length) * 10) / 10;
  }

  // ── Beschikbaarheid ────────────────────────────────────────────────────

  get globalUnavailableDays(): number {
    const total = this.copies.length;
    if (!total || !this.lendings.length) return 0;

    const today = new Date();
    const minTs = Math.min(...this.lendings.map((l) => new Date(l.lendingDate).getTime()));
    const start = new Date(minTs);

    let unavailable = 0;
    const d = new Date(start);
    while (d <= today) {
      const ts = d.getTime();
      const active = this.lendings.filter((l) => {
        const s = new Date(l.lendingDate).getTime();
        const e = l.dateReturned
          ? new Date(l.dateReturned).getTime()
          : l.returned
            ? new Date(l.returnDate).getTime()
            : today.getTime();
        return s <= ts && ts <= e;
      }).length;
      if (active >= total) unavailable++;
      d.setDate(d.getDate() + 1);
    }
    return unavailable;
  }

  get peakMonth(): string {
    if (!this.lendingsByMonth.length) return '—';
    const peak = this.lendingsByMonth.reduce((m, c) => (c.count > m.count ? c : m));
    return `${this.formatMonth(peak.month)} (${peak.count}×)`;
  }

  get lendingsByMonth(): { month: string; count: number }[] {
    const map = new Map<string, number>();
    for (const l of this.lendings) {
      const d = new Date(l.lendingDate);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      map.set(key, (map.get(key) ?? 0) + 1);
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .slice(-6)
      .map(([month, count]) => ({ month, count }));
  }

  get maxMonthCount(): number {
    return Math.max(...this.lendingsByMonth.map((m) => m.count), 1);
  }

  // ── Campus stats ───────────────────────────────────────────────────────

  get campusStats(): CampusStats[] {
    if (!this.copies.length) return [];

    const campusMap = new Map<string, SchoolItemCopy[]>();
    for (const copy of this.copies) {
      const name = copy.campus?.name ?? 'Geen locatie';
      if (!campusMap.has(name)) campusMap.set(name, []);
      campusMap.get(name)!.push(copy);
    }

    const today = new Date();

    return Array.from(campusMap.entries())
      .map(([name, copiesHere]) => {
        const barcodes = new Set(copiesHere.map((c) => c.barcode));
        const campusLendings = this.lendings.filter((l) => barcodes.has(l.copyBarcode));
        const activeLendings = campusLendings.filter((l) => !l.returned).length;
        const returnedWithDates = campusLendings.filter((l) => l.returned && l.dateReturned);
        const avgDuration = returnedWithDates.length
          ? Math.round(
              returnedWithDates.reduce((s, l) => {
                return s + (new Date(l.dateReturned!).getTime() - new Date(l.lendingDate).getTime()) / 86_400_000;
              }, 0) / returnedWithDates.length,
            )
          : 0;

        // Unavailability days per campus
        let unavailableDays = 0;
        if (campusLendings.length) {
          const minTs = Math.min(...campusLendings.map((l) => new Date(l.lendingDate).getTime()));
          const d = new Date(minTs);
          while (d <= today) {
            const ts = d.getTime();
            const active = campusLendings.filter((l) => {
              const s = new Date(l.lendingDate).getTime();
              const e = l.dateReturned
                ? new Date(l.dateReturned).getTime()
                : l.returned
                  ? new Date(l.returnDate).getTime()
                  : today.getTime();
              return s <= ts && ts <= e;
            }).length;
            if (active >= copiesHere.length) unavailableDays++;
            d.setDate(d.getDate() + 1);
          }
        }

        return {
          name,
          totalCopies: copiesHere.length,
          availableCopies: copiesHere.filter((c) => c.copy_status === 'BESCHIKBAAR').length,
          lendingCount: campusLendings.length,
          activeLendings,
          avgDuration,
          lendingsPerCopy:
            copiesHere.length ? Math.round((campusLendings.length / copiesHere.length) * 10) / 10 : 0,
          unavailableDays,
        };
      })
      .sort((a, b) => b.lendingCount - a.lendingCount);
  }

  campusBadge(s: CampusStats): 'onbenut' | null {
    return s.lendingCount === 0 ? 'onbenut' : null;
  }

  campusBadgeLabel(badge: ReturnType<BookReportPage['campusBadge']>): string {
    return badge === 'onbenut' ? 'Niet gebruikt' : '';
  }

  // ── Recente uitleningen ────────────────────────────────────────────────

  get recentLendings(): Lending[] {
    return [...this.lendings]
      .sort((a, b) => new Date(b.lendingDate).getTime() - new Date(a.lendingDate).getTime())
      .slice(0, 8);
  }

  lendingStatus(l: Lending): 'returned' | 'overdue' | 'active' {
    if (l.returned) return 'returned';
    if (new Date() > new Date(l.returnDate)) return 'overdue';
    return 'active';
  }

  lendingStatusLabel(l: Lending): string {
    const s = this.lendingStatus(l);
    if (s === 'returned') return 'Teruggebracht';
    if (s === 'overdue') return 'Te laat';
    return 'Actief';
  }

  // ── Helpers ────────────────────────────────────────────────────────────

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
