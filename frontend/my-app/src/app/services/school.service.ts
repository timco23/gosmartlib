import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { School } from '../../types/types';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SchoolService {
  private apiUrl = `${environment.apiUrl}/schools`;
  
  constructor(private http: HttpClient) {}

  getAll(): Observable<School[]> {
    return this.http.get<School[]>(this.apiUrl);
  }
  create(school: School): Observable<School> {
    return this.http.post<School>(this.apiUrl, school);
  }
  delete(school: School): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${school.id}`);
  }
  update(school: School): Observable<School> {
    return this.http.put<School>(`${this.apiUrl}/${school.id}`, school);
  }
}