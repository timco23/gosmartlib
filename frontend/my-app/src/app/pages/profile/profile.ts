import { Component, OnInit, ChangeDetectorRef  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../auth.service';
import { environment } from '../../../environments/environment';
import { UserProfile } from '../../../types/types';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  profile: UserProfile | null = null;
  loading = true;
  error = '';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    const currentUser = this.authService.currentUserValue;
    if (currentUser && currentUser.email) {
      this.loadProfile(currentUser.email);
    } else {
      this.error = 'Geen gebruiker ingelogd';
      this.loading = false;
    }
  }

  loadProfile(email: string) {
    this.http.get<UserProfile>(`${environment.apiUrl}/user/profile?email=${email}`)
      .subscribe({
        next: (data) => {
          this.profile = data;
          this.loading = false;
          this.cdr.detectChanges();  
        },
        error: (err) => {
          console.error('Profile load error:', err);
          this.error = 'Kon profiel niet laden';
          this.loading = false;
          this.cdr.detectChanges();  // FORCE UPDATE!
        }
      });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('nl-BE');
  }
}