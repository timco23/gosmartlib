import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-review-report-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-report-component.html',
  styleUrl: './review-report-component.css',
})
export class ReviewReportComponent {
  @Input() reviewId!: number;
  @Input() visible = false;
  @Output() submitReport = new EventEmitter<{ reviewId: number; reason?: string }>();
  @Output() cancel = new EventEmitter<void>();
  reason = '';

  onSubmit() {
    this.submitReport.emit({ reviewId: this.reviewId, reason: this.reason?.trim() || undefined });
    this.reason = '';
  }
}
