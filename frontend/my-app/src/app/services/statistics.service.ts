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

  getStudentStatistics(userId: number, schoolId: number, schoolYear?: string): Observable<StudentStatisticsDTO> {
    const params: Record<string, string | number> = { schoolId };
    if (schoolYear !== undefined) params['schoolYear'] = schoolYear;
    return this.http.get<StudentStatisticsDTO>(`${this.apiUrl}/student/${userId}`, { params });
  }

  getClassStatistics(classId: number, schoolId: number, schoolYear?: string): Observable<ClassStatisticsDTO> {
    const params: Record<string, string | number> = { schoolId };
    if (schoolYear !== undefined) params['schoolYear'] = schoolYear;
    return this.http.get<ClassStatisticsDTO>(`${this.apiUrl}/class/${classId}`, { params });
  }

  getAvailableSchoolYears(schoolId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/school-years`, { params: { schoolId } });
  }
}
