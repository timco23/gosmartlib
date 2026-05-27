import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StudentStatisticsDTO } from '../../types/types';
import { StatisticsService } from '../services/statistics.service';
import { UserService } from '../services/user.service';
import { formatDate, lendingStatus, lendingStatusLabel, readingLevelBars } from '../shared/statistics-utils';
import { SchoolYearSelectorComponent } from '../widgets/school-year-selector/school-year-selector';

@Component({
  selector: 'app-student-statistics-page',
  imports: [RouterLink, CommonModule, SchoolYearSelectorComponent],
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
  schoolId!: number;
  selectedYear: string | null = null;
  stats: StudentStatisticsDTO | null = null;
  loading = true;

  readonly lendingStatus = lendingStatus;
  readonly lendingStatusLabel = lendingStatusLabel;
  readonly formatDate = formatDate;

  get readingLevelBars() {
    return readingLevelBars(this.stats?.readingLevelDistribution ?? {});
  }

  ngOnInit(): void {
    this.classId = Number(this.route.snapshot.paramMap.get('classId'));
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));
    this.userApi.getUser().subscribe((user) => {
      if (user.schoolId) {
        this.schoolId = user.schoolId;
        this.loadStats();
      }
    });
  }

  onYearChange(year: string | null): void {
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
}
