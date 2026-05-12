import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookStatisticsDTO } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class StatisticsService {
  private apiUrl = `${environment.apiUrl}/statistics`;

  constructor(private http: HttpClient) {}

  getBookStatistics(bookId: number, schoolId: number): Observable<BookStatisticsDTO> {
    return this.http.get<BookStatisticsDTO>(`${this.apiUrl}/book/${bookId}/statistics`, {
      params: { schoolId },
    });
  }
}
