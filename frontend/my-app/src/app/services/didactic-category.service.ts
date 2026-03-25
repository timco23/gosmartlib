import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { DidacticCategory } from '../../types/types';

@Injectable({
  providedIn: 'root'
})
export class DidacticCategoryService {
  private apiUrl = `${environment.apiUrl}/didacticcategories`;

  constructor(private http: HttpClient) { }

  getDidacticCategories() {
    return this.http.get<DidacticCategory[]>(`${this.apiUrl}`);
  }

  addDidacticCategory(name: string) {
    return this.http.post<DidacticCategory>(`${this.apiUrl}`, { name });
  }

  updateDidacticCategory(id: number, name: string) {
    return this.http.put<DidacticCategory>(`${this.apiUrl}/${id}`, { name });
  }

  deleteDidacticCategory(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}