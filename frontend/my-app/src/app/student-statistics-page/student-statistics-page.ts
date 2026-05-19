import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StudentRecentLendingDTO, StudentStatisticsDTO } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-student-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './student-statistics-page.html',
  styleUrl: './student-statistics-page.css',
})
export class StudentStatisticsPage implements OnInit {
  private route = inject(ActivatedRoute);
  private statisticsApi = inject(StatisticsService);
  private userApi = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  classId!: number;
  userId!: number;
  stats: StudentStatisticsDTO | null = null;
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
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));

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
    this.statisticsApi.getStudentStatistics(this.userId, this.schoolId, this.selectedYear ?? undefined).subscribe((s) => {
      this.stats = s;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  lendingStatus(l: StudentRecentLendingDTO): 'returned' | 'overdue' | 'active' {
    if (l.returned) return 'returned';
    if (new Date() > new Date(l.returnDate)) return 'overdue';
    return 'active';
  }

  lendingStatusLabel(l: StudentRecentLendingDTO): string {
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
