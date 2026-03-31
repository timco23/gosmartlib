import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Lending } from '../../types/types';

@Injectable({
  providedIn: 'root'
})
export class LendingService {
  private apiUrl = `${environment.apiUrl}/lendings`;

  constructor(private http: HttpClient) { }

  getLendings(): Observable<Lending[]> {
    return this.http.get<Lending[]>(this.apiUrl);
  }

  createLending(lending: any) {
    return this.http.post(this.apiUrl, lending);
  }

  updateLending(id: number, lending: any) {
    return this.http.put(`${this.apiUrl}/${id}`, lending);
  }

  deleteLending(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getLendingById(id: number) {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}
