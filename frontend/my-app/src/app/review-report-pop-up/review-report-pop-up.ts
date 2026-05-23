import { Component, effect, inject, Input, model, OnInit, Renderer2, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReviewService } from '../services/review.service';
import { ReviewReport } from '../../types/types';

@Component({
  selector: 'app-review-report-pop-up',
  imports: [DatePipe],
  templateUrl: './review-report-pop-up.html',
  styleUrl: './review-report-pop-up.css',
})
export class ReviewReportPopUp {
  shown = model<boolean>(false);
  reports: ReviewReport[] = [];
  loadingReports = signal<boolean>(true);
  loadingReportsError = signal<boolean>(false);
  @Input() reviewId: number = 0;

  private readonly reviewService = inject(ReviewService);
  private readonly renderer = inject(Renderer2);

  constructor() {
    effect(() => {
      if (this.shown()) {
        this.renderer.addClass(document.body, 'no-scroll');
        this.loadReports();
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

  closePopup(): void {
    this.shown.set(false);
  }

  loadReports(){
    this.loadingReports.set(true)
    this.loadingReportsError.set(false)
    this.reviewService.getReviewReportsByReviewId(this.reviewId).subscribe({
      next: (data) => {
        this.reports = data;
        this.loadingReports.set(false);
      },
      error: () => this.loadingReportsError.set(true)
    })
  }

}
