import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SchoolStatisticsDTO, UserProfile } from '../../types/types';
import { BaseStatisticsPage } from '../shared/base-statistics-page';
import { formatChartLabel, formatDate, lendingStatus, lendingStatusLabel } from '../shared/statistics-utils';

@Component({
  selector: 'app-school-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './school-statistics-page.html',
  styleUrl: './school-statistics-page.css',
})
export class SchoolStatisticsPage extends BaseStatisticsPage {
  stats: SchoolStatisticsDTO | null = null;
  schoolName = '';

  readonly lendingStatus = lendingStatus;
  readonly lendingStatusLabel = lendingStatusLabel;
  readonly formatDate = formatDate;
  readonly formatChartLabel = formatChartLabel;

  protected override onUserLoaded(user: UserProfile): void {
    this.schoolName = user.schoolName ?? '';
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

  protected override loadStats(): void {
    this.loading = true;
    this.statisticsApi.getSchoolStatistics(this.schoolId, this.selectedYear ?? undefined).subscribe((s) => {
      this.stats = s;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }
}
