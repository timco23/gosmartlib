import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Review, CreateReview, CreateReviewReport, ReviewReport, Page } from '../../types/types';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly apiBaseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getReviews(itemId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiBaseUrl}/reviews/books/${itemId}`);
  }

  createReview(itemId: number, review: CreateReview): Observable<Review> {
    return this.http.post<Review>(`${this.apiBaseUrl}/reviews/books/${itemId}`, review);
  }

  deleteReview(itemId: number, reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/reviews/${reviewId}/books/${itemId}`);
  }

  deleteAllReviews(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/reviews/books/${itemId}`);
  }

  reportReview(itemId: number, reviewId: number, payload: CreateReviewReport): Observable<void> {
    return this.http.post<void>(
      `${this.apiBaseUrl}/reviews/${reviewId}/books/${itemId}/reports`,
      payload,
    );
  }

  getReviewsPaginated(schoolId: number, itemId: number, userId: number, reportStatus: string = "ALL", page: number = 0 , newestFirst: boolean = true): Observable<Page<Review>> {
    let params = new HttpParams()
      .set('page', page)
      .set('reportStatus', reportStatus)
      .set('newestFirst', newestFirst);
    if (userId != 0) params.set('userId', userId)
    if (itemId != 0) params.set('itemId', itemId)

    return this.http.get<Page<Review>>(`${this.apiBaseUrl}/reviews/schools/${schoolId}`, { params });
  }

  getReviewReportsByReviewId(reviewId: number): Observable<ReviewReport[]> {
    return this.http.get<ReviewReport[]>(`${this.apiBaseUrl}/reviews/${reviewId}/reports`);
  }

  approveReview(reviewId: number): Observable<ReviewReport[]> {
    return this.http.put<ReviewReport[]>(`${this.apiBaseUrl}/reviews/${reviewId}/approve`, {});
  }
}
