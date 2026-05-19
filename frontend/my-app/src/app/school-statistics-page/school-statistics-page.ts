import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SchoolRecentLendingDTO, SchoolStatisticsDTO } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-school-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './school-statistics-page.html',
  styleUrl: './school-statistics-page.css',
})
export class SchoolStatisticsPage implements OnInit {
  private statisticsApi = inject(StatisticsService);
  private userApi = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  stats: SchoolStatisticsDTO | null = null;
  schoolName = '';
  loading = true;
  availableYears: string[] = [];
  selectedYear: string | null = null;
  private schoolId!: number;

  get currentSchoolYear(): string {
    const now = new Date();
    const year = now.getMonth() >= 8 ? now.getFullYear() : now.getFullYear() - 1;
    return `${year}-${year + 1}`;
  }

  get yearLabel(): string {
    if (this.selectedYear === 'all') return 'Alle jaren';
    return this.selectedYear ?? this.availableYears[0] ?? this.currentSchoolYear;
  }

  isYearActive(year: string, index: number): boolean {
    return this.selectedYear === year || (this.selectedYear === null && index === 0);
  }

  get chartTitle(): string {
    return this.selectedYear === 'all'
      ? 'Ontleningen per schooljaar'
      : `Ontleningen per maand — ${this.yearLabel}`;
  }

  get maxChartCount(): number {
    return Math.max(...(this.stats?.lendingsTrend.map(d => d.count) ?? [1]), 1);
  }

  formatChartLabel(key: string): string {
    const parts = key.split('-');
    if (parts.length !== 2) return key;
    const [a, b] = parts;
    if (b.length === 4) return key; // school year: YYYY-YYYY
    return new Date(+a, +b - 1).toLocaleDateString('nl-BE', { month: 'short', year: '2-digit' });
  }

  ngOnInit(): void {
    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
        this.schoolName = user.schoolName ?? '';
        this.statisticsApi.getAvailableSchoolYears(this.schoolId).subscribe((years) => {
          this.availableYears = years;
          this.cdr.detectChanges();
        });
        this.loadStats();
      }
    });
  }

  selectYear(year: string): void {
    this.selectedYear = year;
    this.loadStats();
  }

  private loadStats(): void {
    this.loading = true;
    this.statisticsApi.getSchoolStatistics(this.schoolId, this.selectedYear ?? undefined).subscribe((s) => {
      this.stats = s;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  lendingStatus(l: SchoolRecentLendingDTO): 'returned' | 'overdue' | 'active' {
    if (l.returned) return 'returned';
    if (new Date() > new Date(l.returnDate)) return 'overdue';
    return 'active';
  }

  lendingStatusLabel(l: SchoolRecentLendingDTO): string {
    const s = this.lendingStatus(l);
    if (s === 'returned') return 'Teruggebracht';
    if (s === 'overdue') return 'Te laat';
    return 'Actief';
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
