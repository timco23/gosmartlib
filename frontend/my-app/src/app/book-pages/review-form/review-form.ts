import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreateReview } from '../../../types/types';
import { StarDisplayComponent } from '../../widgets/star-display/star-display.component';

@Component({
  selector: 'app-review-form',
  imports: [FormsModule, StarDisplayComponent],
  templateUrl: './review-form.html',
  styleUrl: './review-form.css',
})
export class ReviewForm {
  @Output() submitted = new EventEmitter<CreateReview>();
  @Output() cancelled = new EventEmitter<void>();

  stars = 0;
  hoverStar = 0;
  description = '';
  isSubmitting = false;
  errorMessage = '';
  isAnonymous = false;

  submit(): void {
    this.errorMessage = '';

    if (this.stars === 0) {
      this.errorMessage = 'Kies eerst een aantal sterren.';
      return;
    }

    const description = this.description.trim();

    if (description.length > 800) {
      this.errorMessage = 'Beschrijving mag maximaal 800 karakters bevatten.';
      return;
    }

    this.isSubmitting = true;

    this.submitted.emit({
      stars: this.stars,
      description: description.length ? description : undefined,
      isAnonymous: this.isAnonymous,
    });

    this.isSubmitting = false;
  }

  cancel(): void {
    this.errorMessage = '';
    this.cancelled.emit();
  }
}
