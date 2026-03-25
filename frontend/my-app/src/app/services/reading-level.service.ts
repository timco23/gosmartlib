import { Injectable } from '@angular/core';
import { Readinglevel } from '../../types/types';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReadingLevelService {

  private apiUrl = `${environment.apiUrl}/readinglevels`;
  
  constructor(private http: HttpClient) {}

  getAll(): Observable<Readinglevel[]> {
    return this.http.get<Readinglevel[]>(this.apiUrl);
  }
}
