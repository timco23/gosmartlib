import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page, UserProfile } from '../../types/types';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  //email input is ignored
  getUser(_email?: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiUrl}/user/profile`);
  }

  getProfileByEmail(email: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(
      `${environment.apiUrl}/user/profile/${encodeURIComponent(email)}`,
    );
  }

  getFavorites(): Observable<any[]> {
    const stored = localStorage.getItem('currentUser');
    if (!stored)
      return new Observable((o) => {
        o.next([]);
        o.complete();
      });

    const user = JSON.parse(stored);
    return this.http.get<any[]>(`${environment.apiUrl}/user/favorites`, {
      params: { email: user.email },
    });
  }

  getWishlistedBooks(): Observable<any[]> {
    const stored = localStorage.getItem('currentUser');
    if (!stored)
      return new Observable((o) => {
        o.next([]);
        o.complete();
      });

    const user = JSON.parse(stored);
    return this.http.get<any[]>(`${environment.apiUrl}/user/wishlist`, {
      params: { email: user.email },
    });
  }

  getFromSchool(schoolId: number): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${environment.apiUrl}/schools/users`, {
      params: { schoolId: schoolId.toString() },
    });
  }

  getSchoolUsers(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${environment.apiUrl}/user/school-users`);
  }

  addBorrowedItem(userId: number): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/user/addBorrowed`, null, {
      params: { userId: userId.toString() },
    });
  }

  removeBorrowedItem(userId: number): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/user/removeBorrowed`, null, {
      params: { userId: userId.toString() },
    });
  }

  changeSchool(email: string, schoolId: number): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/school`, null, {
      params: { email, schoolId: schoolId.toString() },
    });
  }

  updateUserRole(email: string, role: string): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/role`, null, {
      params: { email, role },
    });
  }

  requestDeletion(): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.apiUrl}/request-deletion`, null);
  }

  cancelDeletion(): Observable<UserProfile> {
    return this.http.delete<UserProfile>(`${this.apiUrl}/cancel-deletion`);
  }

  getFilteredUsers(page: number, nameFilter?: string): Observable<Page<UserProfile>> {
    let params = new HttpParams()
    params.set("page", page)
    if (nameFilter) params.set("filter", nameFilter)
    
    return this.http.get<Page<UserProfile>>(`${this.apiUrl}/filteredUsers`, { params });
  }
}
