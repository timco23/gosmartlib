import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SchoolStatisticsDTO, UserProfile } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';
import { formatChartLabel, formatDate, lendingStatus, lendingStatusLabel } from '../shared/statistics-utils';
import { SchoolYearSelectorComponent } from '../widgets/school-year-selector/school-year-selector';

@Component({
  selector: 'app-school-statistics-page',
  imports: [RouterLink, CommonModule, SchoolYearSelectorComponent],
  templateUrl: './school-statistics-page.html',
  styleUrl: './school-statistics-page.css',
})
export class SchoolStatisticsPage {
  private statisticsApi = inject(StatisticsService);
  private userApi = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  schoolId!: number;
  selectedYear: string | null = null;
  stats: SchoolStatisticsDTO | null = null;
  schoolName = '';
  loading = true;

  readonly lendingStatus = lendingStatus;
  readonly lendingStatusLabel = lendingStatusLabel;
  readonly formatDate = formatDate;
  readonly formatChartLabel = formatChartLabel;

  constructor() {
    this.userApi.getUser().subscribe((user: UserProfile) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
        this.schoolName = user.schoolName ?? '';
        this.loadStats();
      }
    });
  }

  get yearLabel(): string {
    if (this.selectedYear === 'all') return 'Alle jaren';
    const now = new Date();
    const year = now.getMonth() >= 8 ? now.getFullYear() : now.getFullYear() - 1;
    return this.selectedYear ?? `${year}-${year + 1}`;
  }

  get chartTitle(): string {
    return this.selectedYear === 'all'
      ? 'Ontleningen per schooljaar'
      : `Ontleningen per maand`;
  }

  get filteredLendingsTrend() {
    return (this.stats?.lendingsTrend ?? []).filter(d => {
      const parts = d.period.split('-');
      if (parts.length === 2 && parts[1].length === 2) {
        const month = +parts[1];
        if ((month === 7 || month === 8) && d.count === 0) return false;
      }
      return true;
    });
  }

  get maxChartCount(): number {
    return Math.max(...this.filteredLendingsTrend.map(d => d.count), 1);
  }

  onYearChange(year: string | null): void {
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
}
