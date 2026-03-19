import { CommonModule } from '@angular/common';
import { Component, OnInit, HostListener } from '@angular/core';
import { RouterOutlet, Router, RouterLink, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';
import { LoginResponse } from '../types/types';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  currentUser: LoginResponse | null = null;
  dropdownOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    // Subscribe to user changes
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    // If a token exists, validate it by calling /auth/me once on startup.
    // If the token is invalid/expired, log out and redirect to /login.
    if (this.authService.isLoggedIn()) {
      this.authService.me().subscribe({
        next: () => {
          if (this.router.url === '/login') this.router.navigate(['/home']);
        },
        error: () => {
          this.authService.logout();
          this.router.navigate(['/login'], { queryParams: { oauth: 'session_expired' } });
        },
      });
    }

    // Route protection based on auth state
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      const currentUrl = this.router.url;
      const isLoggedIn = this.authService.isLoggedIn();

      // If logged in and on login page, redirect to home
      if (isLoggedIn && currentUrl === '/login') {
        this.router.navigate(['/home']);
      }

      // If not logged in and not on login page, redirect to login
      if (!isLoggedIn && currentUrl !== '/login') {
        this.router.navigate(['/login']);
      }
    });
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown() {
    this.dropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.dropdownOpen = false;
    this.router.navigate(['/login']);
  }

  // Sluit dropdown als je buiten klikt
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.dropdownOpen = false;
    }
  }
}
