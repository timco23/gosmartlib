import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Review, CreateReview, CreateReviewReport } from '../../types/types';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly apiBaseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getReviews(itemId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/books/${itemId}/reviews`);
  }

  createReview(itemId: number, review: CreateReview): Observable<Review> {
    return this.http.post<Review>(`${this.apiBaseUrl}/books/${itemId}/reviews`, review);
  }

  deleteReview(itemId: number, reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/books/${itemId}/reviews/${reviewId}`);
  }

  deleteAllReviews(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/books/${itemId}/reviews`);
  }

  reportReview(itemId: number, reviewId: number, payload: CreateReviewReport): Observable<void> {
    return this.http.post<void>(
      `${this.apiBaseUrl}/books/${itemId}/reviews/${reviewId}/reports`,
      payload,
    );
  }

  reportExistsByReviewIdAndReporterId(itemId: number, reviewId: number, reporterId: number): Observable<Boolean>{
    return this.http.get<Boolean>(
      `${this.apiBaseUrl}/books/${itemId}/reviews/${reviewId}/existsFrom/${reporterId}`,
    )
  }
}
