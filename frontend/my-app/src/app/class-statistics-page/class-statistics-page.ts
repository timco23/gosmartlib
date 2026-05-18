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
  private schoolId!: number;

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
        this.statisticsApi.getClassStatistics(this.classId, this.schoolId).subscribe((s) => {
          this.stats = s;
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  lateColor(s: StudentStatisticsDTO): 'ok' | 'warn' {
    return s.lateReturnPercent > 20 ? 'warn' : 'ok';
  }
}
