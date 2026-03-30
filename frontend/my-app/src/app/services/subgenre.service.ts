import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Genre, Subgenre, Tag } from '../../types/types';

@Injectable({
  providedIn: 'root'
})
export class SubgenreService {
  private apiUrl = `${environment.apiUrl}/subgenres`;

  constructor(private http: HttpClient) { }

  getSubgenres(): Observable<Subgenre[]> {
    return this.http.get<Subgenre[]>(this.apiUrl);
  }

  addSubgenre(name: string, genreId: number): Observable<Subgenre> {
    return this.http.post<Subgenre>(this.apiUrl, { name });
  }

  updateSubgenre(id: number, name: string): Observable<Subgenre> {
    return this.http.put<Subgenre>(`${this.apiUrl}/${id}`, { name });
  }

  deleteSubgenre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getSubgenresByGenres(genreIds: number[]): Observable<Subgenre[]> {
    const params = genreIds.map(id => `genreIds=${id}`).join('&');
    return this.http.get<Subgenre[]>(`${this.apiUrl}/genre?${params}`);
  }
}
