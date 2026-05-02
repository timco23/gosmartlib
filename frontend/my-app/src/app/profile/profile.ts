import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserProfile } from '../../types/types';
import { UserService } from '../services/user.service';

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
export class Profile implements OnInit {
  profile: UserProfile | null = null;
  loading: boolean = true;
  error: string = '';

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private userService: UserService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const email = params['email'] ?? this.authService.getCurrentUser()?.email;
      if (!email) {
        this.error = 'Geen gebruiker ingelogd';
        this.loading = false;
        return;
      }
      this.loadProfile(email);
    });
  }

  loadProfile(email: string): void {
    this.userService.getUser(email).subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.error = 'Kon profiel niet laden';
        this.loading = false;
      },
    });
  }
}
