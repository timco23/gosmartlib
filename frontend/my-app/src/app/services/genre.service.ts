import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Genre } from '../../types/types';

@Injectable({
  providedIn: 'root'
})
export class GenreService {
  private apiUrl = `${environment.apiUrl}/genres`;

  constructor(private http: HttpClient) { }

  getGenres() {
    return this.http.get<Genre[]>(`${this.apiUrl}`);
  }

  addGenre(name: string) {
    return this.http.post<Genre>(`${this.apiUrl}`, { name });
  }

  updateGenre(id: number, name: string) {
    return this.http.put<Genre>(`${this.apiUrl}/${id}`, { name });
  }

  deleteGenre(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
