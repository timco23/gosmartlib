import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { School } from '../school-management/school-management.component';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class SchoolService {
  private apiUrl = 'http://localhost/api/schools';

  constructor(private http: HttpClient) {}

  getAll(): Observable<School[]> {
    return this.http.get<School[]>(this.apiUrl);
  }

  create(school: School): Observable<School> {
    return this.http.post<School>(this.apiUrl, school);
  }
}
