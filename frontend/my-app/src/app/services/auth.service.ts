import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, Role } from '../../types/types';

const TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

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

  getOAuthLoginUrl(): string {
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return `${base}/oauth/smartschool/login`;
  }

  //For staying logged in:

  //Sets jWT token
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  //Reads JWT token
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * Logs out the current user.
   * Removes user data from localStorage and clears currentUser$.
   */
  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
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

  setCurrentUser(user: LoginResponse): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  me(): Observable<LoginResponse> {
    //backend call
    return this.http.get<LoginResponse>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => {
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      }),
    );
  }

  fetchCurrentUser(): Observable<LoginResponse> {
    return this.me();
  }

  /**
   * Refreshes the JWT token using the refresh token.
   * Called when JWT is expired or about to expire.
   * @returns Observable with new JWT token
   */
  refreshToken(): Observable<{ token: string }> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http
      .post<{ token: string }>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(
        tap((response) => {
          this.setToken(response.token);
        }),
      );
  }

  /**
   * Checks if a user is currently logged in.
   * @returns true if user is logged in, false otherwise
   */
  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  atLeastHasRole(role: Role): boolean {
    const user = this.currentUserValue;
    if (!user?.role) return false;

    const key = user.role.toUpperCase() as keyof typeof Role;
    const currentRole = Role[key];
    if (currentRole === undefined) return false;

    return role <= currentRole;
  }
}
