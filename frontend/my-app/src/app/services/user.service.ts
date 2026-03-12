import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CreateUserRequest, UserProfile } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) { }

  /**
   * Creates a new student user.
   * @param request student details
   * @returns created user profile
   */
  createUser(request: CreateUserRequest): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.apiUrl}/create`, request);
  }

  getUser(email: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/user/profile`, {
      params: { email: email }
    })
  }

  getFavorites(): Observable<any[]> {
    const stored = localStorage.getItem('currentUser');
    if (!stored) return new Observable(o => { o.next([]); o.complete(); });

    const user = JSON.parse(stored);
    return this.http.get<any[]>(`${environment.apiUrl}/user/favorites`, {
      params: { email: user.email }
    });
  }
}
