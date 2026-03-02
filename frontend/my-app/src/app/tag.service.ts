import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Tag } from '../types/types';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private url = 'http://localhost:8080/api/tags';

  constructor(private http: HttpClient) {}

  getTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.url);
  }

  addTag(name: string): Observable<Tag> {
    return this.http.post<Tag>(this.url, { name });
  }

  updateTag(id: number, name: string): Observable<Tag> {
    return this.http.put<Tag>(`${this.url}/${id}`, { name });
  }

  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
