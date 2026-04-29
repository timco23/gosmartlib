import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Review } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { ReviewReportComponent } from '../../review-report-component/review-report-component';
import { StarDisplayComponent } from '../../star-display/star-display.component';

@Component({
  selector: 'app-review-list',
  imports: [CommonModule, FormsModule, ReviewReportComponent, StarDisplayComponent],
  templateUrl: './review-list.html',
  styleUrl: './review-list.css',
})
export class ReviewList {
  @Input() reviews: Review[] = [];
  @Input() userRole: string = '';
  @Input() currentUserName: string = '';

  @Output() deleteReview = new EventEmitter<number>();
  @Output() deleteAll = new EventEmitter<void>();
  @Output() reportReview = new EventEmitter<{ reviewId: number; reason?: string }>();

  confirmMessage: string | null = null;
  private pendingAction: (() => void) | null = null;
  activeReportReviewId: number | null = null;
  reportReason = '';

  constructor(private authService: AuthService) {}

  getDisplayName(review: Review): string {
    // Admins and Library Administrators ALWAYS see the real name
    if (this.isAdminOrBeheerder) {
      return review.authorName;
    }

    // Regular users see "Anoniem" if it is an anonymous review
    if (review.isAnonymous) {
      return 'Anoniem';
    }

    return review.authorName;
  }

  canDelete(review: Review): boolean {
    const u: any = this.authService.getCurrentUser?.();
    if (!u) return false;

    const role = (u.role ?? '').toUpperCase();
    const isAdminOrBib = role === 'ADMIN' || role === 'BIBLIOTHEEKBEHEERDER';

    const userEmail = (u.email ?? '').toLowerCase();
    const userSs = (
      u.smartschoolUsername ??
      u.smartschool_username ??
      u.username ??
      ''
    ).toLowerCase();

    const ownerByEmail = (review.authorEmail ?? '').toLowerCase() === userEmail;
    const ownerBySs = !!userSs && (review.authorSmartschoolUsername ?? '').toLowerCase() === userSs;

    return isAdminOrBib || ownerByEmail || ownerBySs;
  }

  get isAdminOrBeheerder(): boolean {
    const r = this.userRole?.toUpperCase();
    return r === 'ADMIN' || r === 'BIBLIOTHEEKBEHEERDER';
  }

  onDeleteReview(review: Review): void {
    if (!this.canDelete(review)) return;

    this.confirmMessage = 'Weet u zeker dat u deze review wilt verwijderen?';
    this.pendingAction = () => this.deleteReview.emit(review.id);
  }

  onDeleteAll(): void {
    if (!this.isAdminOrBeheerder) return;

    this.confirmMessage = 'Weet u zeker dat u alle reviews wilt verwijderen?';
    this.pendingAction = () => this.deleteAll.emit();
  }

  confirmAction(): void {
    this.pendingAction?.();
    this.cancelConfirm();
  }

  cancelConfirm(): void {
    this.confirmMessage = null;
    this.pendingAction = null;
  }

  private isOwner(review: Review): boolean {
    const u: any = this.authService.getCurrentUser?.();
    if (!u) return false;

    const userEmail = (u.email ?? '').toLowerCase();
    const userSs = (
      u.smartschoolUsername ??
      u.smartschool_username ??
      u.username ??
      ''
    ).toLowerCase();

    const ownerByEmail = (review.authorEmail ?? '').toLowerCase() === userEmail;
    const ownerBySs = !!userSs && (review.authorSmartschoolUsername ?? '').toLowerCase() === userSs;
    return ownerByEmail || ownerBySs;
  }

  canReport(review: Review): boolean {
    return !this.isOwner(review);
  }

  openReportForm(reviewId: number): void {
    this.activeReportReviewId = this.activeReportReviewId === reviewId ? null : reviewId;
  }

  cancelReportForm(): void {
    this.activeReportReviewId = null;
  }

  submitReportFromComponent(event: { reviewId: number; reason?: string }): void {
    this.reportReview.emit(event);
    this.cancelReportForm();
  }
}
