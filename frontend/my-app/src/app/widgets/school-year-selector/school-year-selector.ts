import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatisticsService } from '../../services/statistics.service';

@Component({
  selector: 'app-school-year-selector',
  imports: [CommonModule],
  templateUrl: './school-year-selector.html',
  styleUrl: './school-year-selector.css',
})
export class SchoolYearSelectorComponent implements OnChanges {
  @Input() schoolId!: number;
  @Output() yearChange = new EventEmitter<string | null>();

  availableYears: string[] = [];
  selectedYear: string | null = null;

  constructor(private statisticsApi: StatisticsService) {}

  ngOnChanges(): void {
    if (!this.schoolId) return;
    this.statisticsApi.getAvailableSchoolYears(this.schoolId).subscribe((years) => {
      this.availableYears = years;
    });
  }

  get yearLabel(): string {
    if (this.selectedYear === 'all') return 'Alle jaren';
    return this.selectedYear ?? this.availableYears[0] ?? this.currentSchoolYear;
  }

  get currentSchoolYear(): string {
    const now = new Date();
    const year = now.getMonth() >= 8 ? now.getFullYear() : now.getFullYear() - 1;
    return `${year}-${year + 1}`;
  }

  isYearActive(year: string, index: number): boolean {
    return this.selectedYear === year || (this.selectedYear === null && index === 0);
  }

  selectYear(year: string): void {
    this.selectedYear = year;
    this.yearChange.emit(year);
  }
}
