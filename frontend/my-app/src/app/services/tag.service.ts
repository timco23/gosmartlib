import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Tag } from '../../types/types';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private apiUrl = `${environment.apiUrl}/tags`;

  constructor(private http: HttpClient) {}

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.apiUrl);
  }

  addTag(name: string): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, { name });
  }

  updateTag(id: number, name: string): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}`, { name });
  }

  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
