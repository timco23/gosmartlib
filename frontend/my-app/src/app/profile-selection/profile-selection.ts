import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserProfile, Role } from '../../types/types';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-profile-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-selection.html',
  styleUrl: './profile-selection.css',
})
export class ProfileSelectionComponent implements OnInit {
  users: UserProfile[] = [];
  filteredUsers: UserProfile[] = [];
  emailFilter: string = '';
  loading: boolean = true;
  error: string = '';
  isAdmin: boolean = false;

  constructor(private userService: UserService, private router: Router, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.atLeastHasRole(Role.ADMIN);
    this.userService.getSchoolUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load school users', err);
        this.error = 'Kon gebruikers niet laden';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  onFilterChange(): void {
    const filter = this.emailFilter.toLowerCase().trim();
    this.filteredUsers = filter
      ? this.users.filter(u => u.email?.toLowerCase().includes(filter))
      : this.users;
  }

  openProfile(user: UserProfile): void {
    this.router.navigate(['/profile'], { queryParams: { email: user.email } });
  }
}
