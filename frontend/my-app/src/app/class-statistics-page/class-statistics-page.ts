import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ClassStatisticsDTO, StudentStatisticsDTO } from '../../types/types';
import { BaseStatisticsPage } from '../shared/base-statistics-page';
import { readingLevelBars } from '../shared/statistics-utils';

@Component({
  selector: 'app-class-statistics-page',
  imports: [RouterLink, CommonModule],
  templateUrl: './class-statistics-page.html',
  styleUrl: './class-statistics-page.css',
})
export class ClassStatisticsPage extends BaseStatisticsPage implements OnInit {
  private route = inject(ActivatedRoute);

  classId!: number;
  stats: ClassStatisticsDTO | null = null;

  get readingLevelBars() {
    return readingLevelBars(this.stats?.readingLevelDistribution ?? {});
  }

  override ngOnInit(): void {
    this.classId = Number(this.route.snapshot.paramMap.get('classId'));
    this.initSchoolYear();
  }

  protected override loadStats(): void {
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
