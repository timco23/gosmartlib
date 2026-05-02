import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
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
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const email = params['email'];
      if (email) {
        this.loadProfileByEmail(email);
      } else {
        this.loadOwnProfile();
      }
    });
  }

  loadOwnProfile(): void {
    this.userService.getUser().subscribe({
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

  loadProfileByEmail(email: string): void {
    this.userService.getProfileByEmail(email).subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 403) {
          this.router.navigate(['/forbidden']);
        } else {
          console.error('Failed to load profile', err);
          this.error = 'Kon profiel niet laden';
          this.loading = false;
        }
      },
    });
  }
}
