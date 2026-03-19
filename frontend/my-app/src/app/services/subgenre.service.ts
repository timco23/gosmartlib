import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subgenre } from '../../types/types';

@Injectable({
  providedIn: 'root'
})
export class SubgenreService {
  private apiUrl = `${environment.apiUrl}/subgenres`;

  constructor(private http: HttpClient) {}

  getSubgenres(): Observable<Subgenre[]> {
    return this.http.get<Subgenre[]>(this.apiUrl);
  }

  addSubgenre(name: string): Observable<Subgenre> {
    return this.http.post<Subgenre>(this.apiUrl, { name });
  }

  updateSubgenre(id: number, name: string): Observable<Subgenre> {
    return this.http.put<Subgenre>(`${this.apiUrl}/${id}`, { name });
  }

  deleteSubgenre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
