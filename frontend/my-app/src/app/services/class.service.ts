import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookStatisticsDTO, Class, ClassStatisticsDTO, StudentStatisticsDTO } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class ClassService {
  private apiUrl = `${environment.apiUrl}/classes`;

  constructor(private http: HttpClient) {}

  getClassList(schoolId: number): Observable<Class[]> {
    return this.http.get<Class[]>(`${this.apiUrl}`, {
      params: { schoolId },
    });
  }

  getClassStatistics(classId: number, schoolId: number): Observable<ClassStatisticsDTO> {
    return this.http.get<ClassStatisticsDTO>(`${this.apiUrl}/class/${classId}`, {
      params: { schoolId },
    });
  }
}
