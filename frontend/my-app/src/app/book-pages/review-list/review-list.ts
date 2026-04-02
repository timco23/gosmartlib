import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Review } from '../../../types/types';

@Component({
  selector: 'app-review-list',
  imports: [CommonModule],
  templateUrl: './review-list.html',
  styleUrl: './review-list.css',
})
export class ReviewList {
  @Input() reviews: Review[] = [];
  @Input() userRole: string = '';
  @Input() currentUserName: string = '';

  @Output() deleteReview = new EventEmitter<number>();
  @Output() deleteAll = new EventEmitter<void>();

  confirmMessage: string | null = null;
  private pendingAction: (() => void) | null = null;

  get isAdminOrBeheerder(): boolean {
    const r = this.userRole?.toUpperCase();
    return r === 'ADMIN' || r === 'BIBLIOTHEEKBEHEERDER';
  }

  starsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  canDeleteReview(review: Review): boolean {
    if (this.isAdminOrBeheerder) return true;

    const author = (review.authorName ?? '').trim().toLowerCase();
    const current = (this.currentUserName ?? '').trim().toLowerCase();
    return !!author && !!current && author === current;
  }

  onDeleteReview(review: Review): void {
    if (!this.canDeleteReview(review)) return;

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
}
