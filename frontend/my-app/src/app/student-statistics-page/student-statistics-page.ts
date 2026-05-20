import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StudentStatisticsDTO } from '../../types/types';
import { BaseStatisticsPage } from '../shared/base-statistics-page';
import { formatDate, lendingStatus, lendingStatusLabel, readingLevelBars } from '../shared/statistics-utils';

@Component({
  selector: 'app-student-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './student-statistics-page.html',
  styleUrl: './student-statistics-page.css',
})
export class StudentStatisticsPage extends BaseStatisticsPage implements OnInit {
  private route = inject(ActivatedRoute);

  classId!: number;
  userId!: number;
  stats: StudentStatisticsDTO | null = null;

  readonly lendingStatus = lendingStatus;
  readonly lendingStatusLabel = lendingStatusLabel;
  readonly formatDate = formatDate;

  get readingLevelBars() {
    return readingLevelBars(this.stats?.readingLevelDistribution ?? {});
  }

  override ngOnInit(): void {
    this.classId = Number(this.route.snapshot.paramMap.get('classId'));
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));
    this.initSchoolYear();
  }

  protected override loadStats(): void {
    this.loading = true;
    this.statisticsApi.getStudentStatistics(this.userId, this.schoolId, this.selectedYear ?? undefined).subscribe((s) => {
      this.stats = s;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }
}
