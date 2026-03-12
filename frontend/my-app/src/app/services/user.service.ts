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

  getFavorites(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${environment.apiUrl}/user/favorites`, {
      params: { email: localStorage.getItem('email')! }
    });
  }
}
