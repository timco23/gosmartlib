import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role, UserProfile } from '../../types/types';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  profile: UserProfile | null = null;
  loading: boolean = true;
  error: string = '';

  isViewingOther: boolean = false;
  selectedRole: string = '';
  roleUpdateSuccess: string = '';
  roleUpdateError: string = '';

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.authService.fetchCurrentUser().subscribe({
      next: () => this.cdr.detectChanges()
    });

    this.route.queryParams.subscribe(params => {
      const email = params['email'];
      if (email) {
        this.isViewingOther = true;
        this.loadProfileByEmail(email);
      } else {
        this.isViewingOther = false;
        this.loadOwnProfile();
      }
    });
  }

  get canEditRole(): boolean {
    if (!this.isViewingOther || !this.profile) return false;
    const currentUser = this.authService.currentUserValue;
    if (currentUser?.email === this.profile.email) return false;
    if (!this.authService.atLeastHasRole(Role.BIBLIOTHEEKBEHEERDER)) return false;
    if (!this.authService.atLeastHasRole(Role.ADMIN) && this.profile.role === 'ADMIN') return false;
    return true;
  }

  get availableRoles(): string[] {
    if (this.authService.atLeastHasRole(Role.ADMIN)) {
      return ['LEERLING', 'LEERKRACHT', 'BIBLIOTHEEKBEHEERDER', 'ADMIN'];
    }
    return ['LEERLING', 'LEERKRACHT', 'BIBLIOTHEEKBEHEERDER'];
  }

  updateRole(): void {
    if (!this.profile || !this.selectedRole) return;
    this.roleUpdateSuccess = '';
    this.roleUpdateError = '';

    this.userService.updateUserRole(this.profile.email, this.selectedRole).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.selectedRole = updated.role;
        this.roleUpdateSuccess = 'Rol succesvol bijgewerkt';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.roleUpdateError = err.error ?? 'Kon rol niet bijwerken';
        this.cdr.detectChanges();
      },
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
        this.selectedRole = data.role;
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
