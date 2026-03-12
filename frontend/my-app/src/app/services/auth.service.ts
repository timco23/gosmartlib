import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, Role } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  //BehaviorSubject has a notion of the current value that it stores and emits to all new subscriptions.
  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const user = localStorage.getItem('currentUser');
    if (user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  /**
   * Logs in a user with email and password.
   * Stores user data in localStorage and updates currentUser$.
   * @param email user's email
   * @param password user's password
   * @returns Observable with login response
   */
  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(
        tap((user) => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }),
      );
  }

  /**
   * Logs out the current user.
   * Removes user data from localStorage and clears currentUser$.
   */
  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  /**
   * Gets the current user value (synchronous).
   * @returns current user or null
   */
  get currentUserValue(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Gets the current user (alias for currentUserValue).
   * @returns current user or null
   */
  getCurrentUser(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Checks if a user is currently logged in.
   * @returns true if user is logged in, false otherwise
   */
  isLoggedIn(): boolean {
    return this.currentUserValue !== null;
  }

  atLeastHasRole(role: Role): boolean {
    const currentRole: Role = Role[this.currentUserValue!.role.toUpperCase() as keyof typeof Role];
    return role <= currentRole
  }
}
