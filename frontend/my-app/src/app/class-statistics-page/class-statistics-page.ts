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
