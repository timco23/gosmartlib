import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { CreateReadingListPayload, ReadingList } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class ReadingListService {
  private readonly apiBaseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

    getReadingLists(): Observable<ReadingList[]> {
      return this.http.get<ReadingList[]>(`${this.apiBaseUrl}/lists`);
    }

    getReadingListById(id: number): Observable<ReadingList> {
     return this.http.get<ReadingList>(`${this.apiBaseUrl}/lists/${id}`);
    }

    getReadingListsByUserid(userId: number): Observable<ReadingList[]> {
     return this.http.get<ReadingList[]>(`${this.apiBaseUrl}/lists/user/${userId}`);
    }

    checkIfItemIsInReadingList(itemId: number): Observable<boolean> {
     return this.http.get<boolean>(`${this.apiBaseUrl}/lists/exists/${itemId}`);
    }

    addReadingList(payload: CreateReadingListPayload): Observable<ReadingList> {
     return this.http.post<ReadingList>(`${this.apiBaseUrl}/lists`, payload);
    }

    deleteReadingList(id: number): Observable<ReadingList> {
     return this.http.delete<ReadingList>(`${this.apiBaseUrl}/lists/${id}`);
    }

    updateReadingList(id: number, payload: CreateReadingListPayload): Observable<ReadingList> {
     return this.http.put<ReadingList>(`${this.apiBaseUrl}/lists/${id}`, payload);
    }
}