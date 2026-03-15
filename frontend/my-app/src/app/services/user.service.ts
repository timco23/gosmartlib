import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserProfile } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves user profile by email.
   * @param email user's email address
   * @returns user profile data
   */
  getUser(email: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/user/profile`, {
      params: { email: email },
    });
  }
}
