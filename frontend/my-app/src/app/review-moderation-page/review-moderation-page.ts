import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';
import { Review, School } from '../../types/types';
import { ReviewService } from '../services/review.service';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { SchoolService } from '../services/school.service';

@Component({
  selector: 'app-review-moderation-page',
  imports: [],
  templateUrl: './review-moderation-page.html',
  styleUrl: './review-moderation-page.css',
})
export class ReviewModerationPage implements OnInit {
  reviews: Review[] = [];
  currentPage: number = 0;
  totalPages: number = 0
  school: School | null = null;
  selectedItemId: number = 0;
  selectedUserId: number = 0;
  reportStatus: string = "ALL";
  newestFirst: boolean = true;
  loadingSchool = signal<boolean>(true);
  loadingSchoolError = signal<boolean>(false);
  loadingReviews = signal<boolean>(true);

  reviewService = inject(ReviewService)
  cdr = inject(ChangeDetectorRef)
  authService = inject(AuthService)
  userService = inject(UserService)
  schoolService = inject(SchoolService)

  ngOnInit() {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;

      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (data.schoolName != null) {
            this.schoolService.getByName(data.schoolName).subscribe({
              next: (schoolData) => {
                this.school = schoolData;
                this.loadingSchool.set(false);
                this.loadReviews();
              },
              error: () => this.loadingSchoolError.set(true)
            });
          }
        },
        error: () => this.loadingSchoolError.set(true)
      });
    });
  }

  loadReviews() {
    this.loadingReviews.set(true);
    this.reviewService.getReviewsPaginated(this.school!.id!, this.selectedItemId, this.selectedUserId, this.reportStatus, this.currentPage, this.newestFirst).subscribe({
      next: (data) => {
        this.reviews = data.content;
        this.totalPages = data.totalPages;
        this.loadingReviews.set(false);
      }
    });
  }

  changeSelectedItemId(id: number) {
    this.selectedItemId = id;
    this.loadReviews();
  }

  changeSelectedUserId(id: number) {
    this.selectedUserId = id;
    this.loadReviews();
  }

  clearSelectedItemId() {
    this.selectedItemId = 0;
    this.loadReviews();
  }

  clearSelectedUserId() {
    this.selectedUserId = 0;
    this.loadReviews();
  }

  changeReportStatus(status: string) {
    this.reportStatus = status;
    this.loadReviews();
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadReviews();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadReviews();
    } 
  }

  toggleSortOrder(state: boolean) {
    this.newestFirst = state;
    this.loadReviews();
  }

  deleteReview(id: number, itemId: number) {
    this.reviewService.deleteReview(itemId, id).subscribe({
      next: () => this.loadReviews()
    });
  }

  approveReview(id: number) {
    this.reviewService.approveReview(id).subscribe({
      next: () => this.loadReviews()
    }) 
  }
}
