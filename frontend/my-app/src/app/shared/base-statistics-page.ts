import { ChangeDetectorRef, Directive, inject, OnInit } from '@angular/core';
import { UserProfile } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';

@Directive()
export abstract class BaseStatisticsPage implements OnInit {
  protected statisticsApi = inject(StatisticsService);
  protected userApi = inject(UserService);
  protected cdr = inject(ChangeDetectorRef);

  availableYears: string[] = [];
  selectedYear: string | null = null;
  loading = true;
  protected schoolId!: number;

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

  selectYear(year: string): void {
    this.selectedYear = year;
    this.loadStats();
  }

  protected onUserLoaded(_user: UserProfile): void {}

  protected abstract loadStats(): void;

  ngOnInit(): void {
    this.initSchoolYear();
  }

  protected initSchoolYear(): void {
    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
        this.onUserLoaded(user);
        this.statisticsApi.getAvailableSchoolYears(this.schoolId).subscribe((years) => {
          this.availableYears = years;
          this.cdr.detectChanges();
        });
        this.loadStats();
      }
    });
  }
}
