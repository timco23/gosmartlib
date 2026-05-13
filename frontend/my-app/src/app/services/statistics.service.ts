import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookStatisticsDTO, ClassStatisticsDTO, StudentStatisticsDTO } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class StatisticsService {
  private apiUrl = `${environment.apiUrl}/statistics`;

  constructor(private http: HttpClient) {}

  getBookStatistics(
    bookId: number,
    schoolId: number,
    groupBy: 'month' | 'year' = 'month',
    trendLimit = 6
  ): Observable<BookStatisticsDTO> {
    return this.http.get<BookStatisticsDTO>(`${this.apiUrl}/book/${bookId}`, {
      params: { schoolId, groupBy, trendLimit },
    });
  }

  getStudentStatistics(userId: number, schoolId: number): Observable<StudentStatisticsDTO> {
    return this.http.get<StudentStatisticsDTO>(`${this.apiUrl}/student/${userId}`, {
      params: { schoolId },
    });
  }

  getClassStatistics(classId: number, schoolId: number): Observable<ClassStatisticsDTO> {
    return this.http.get<ClassStatisticsDTO>(`${this.apiUrl}/class/${classId}`, {
      params: { schoolId },
    });
  }
}
