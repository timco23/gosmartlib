import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClassStatisticsDTO, StudentStatisticsDTO } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-class-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './class-statistics-page.html',
  styleUrl: './class-statistics-page.css',
})
export class ClassStatisticsPage implements OnInit {
  private route = inject(ActivatedRoute);
  private statisticsApi = inject(StatisticsService);
  private userApi = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  classId!: number;
  stats: ClassStatisticsDTO | null = null;
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

  get readingLevelBars(): { label: string; count: number; pct: number }[] {
    const distribution = this.stats?.readingLevelDistribution ?? {};
    const levels = ['A', 'B', 'C', 'D'];
    const bars = levels.map(label => ({ label, count: distribution[label] ?? 0, pct: 0 }));
    const max = Math.max(...bars.map(b => b.count), 1);
    for (const bar of bars) {
      bar.pct = Math.round((bar.count / max) * 100);
    }
    return bars;
  }

  ngOnInit(): void {
    this.classId = Number(this.route.snapshot.paramMap.get('classId'));
    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
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
    this.statisticsApi.getClassStatistics(this.classId, this.schoolId, this.selectedYear ?? undefined).subscribe((s) => {
      this.stats = s;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  lateColor(s: StudentStatisticsDTO): 'ok' | 'warn' {
    return s.lateReturnPercent > 20 ? 'warn' : 'ok';
  }
}
