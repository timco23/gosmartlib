import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookStatisticsDTO, Lending, LendingRequest } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class LendingService {
  private apiUrl = `${environment.apiUrl}/lendings`;

  constructor(private http: HttpClient) {}

  getLendings(): Observable<Lending[]> {
    return this.http.get<Lending[]>(this.apiUrl);
  }

  createLending(request: LendingRequest): Observable<any> {
    return this.http.post(this.apiUrl, request);
  }

  updateLending(id: number, lending: Lending): Observable<Lending> {
    return this.http.put<Lending>(`${this.apiUrl}/${id}`, lending);
  }

  deleteLending(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getLendingById(id: number) {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  extendLending(id: number): Observable<Lending> {
    return this.http.post<Lending>(`${this.apiUrl}/${id}/extend`, {});
  }

  getLendingsByUserId(id: number): Observable<Lending[]> {
    return this.http.get<Lending[]>(`${this.apiUrl}/userId`, {
      params: { userId: id },
    });
  }

  getLendingsByUser(email: string): Observable<Lending[]> {
    return this.http.get<Lending[]>(`${this.apiUrl}/user`, {
      params: { email },
    });
  }

  getLendingsByBookId(bookId: number): Observable<Lending[]> {
    return this.http.get<Lending[]>(`${this.apiUrl}/book/${bookId}`);
  }

  getBookStatistics(bookId: number, schoolId: number): Observable<BookStatisticsDTO> {
    return this.http.get<BookStatisticsDTO>(`${this.apiUrl}/book/${bookId}/statistics`, {
      params: { schoolId },
    });
  }
}
