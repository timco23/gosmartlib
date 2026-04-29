import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreateReview } from '../../../types/types';
import { StarDisplayComponent } from '../../star-display/star-display.component';

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
  title = '';
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

    const title = this.title.trim();
    const description = this.description.trim();

    if (title.length > 80) {
      this.errorMessage = 'Titel mag maximaal 80 karakters bevatten.';
      return;
    }

    if (description.length > 800) {
      this.errorMessage = 'Beschrijving mag maximaal 800 karakters bevatten.';
      return;
    }

    this.isSubmitting = true;

    this.submitted.emit({
      stars: this.stars,
      title: title.length ? title : null,
      description: description.length ? description : undefined,
      isAnonymous: this.isAnonymous,
    });

    this.isSubmitting = false;
  }

  cancel() {
    this.cancelled.emit();
  }
}
