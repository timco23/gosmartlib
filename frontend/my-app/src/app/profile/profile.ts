import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';
import { UserProfile } from '../../types/types';

/**
 * Component displaying user profile information.
 * Shows different fields for students vs admins.
 */

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit{
  profile: UserProfile | null = null;
  loading: boolean = true;
  error: string = '';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  /**
   * Loads user profile from backend.
   */
  loadProfile(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email) {
      this.error = 'Geen gebruiker ingelogd';
      this.loading = false;
      return;
    }

    this.http.get<UserProfile>(`${environment.apiUrl}/user/profile`, {
      params: { email: currentUser.email }
    }).subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.error = 'Kon profiel niet laden';
        this.loading = false;
      }
    });
  }

  /**
   * Formats ISO date to readable Dutch format.
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('nl-BE', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }
}