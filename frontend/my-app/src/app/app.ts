import { CommonModule } from '@angular/common';
import { Component, OnInit, HostListener } from '@angular/core';
import { RouterOutlet, Router, RouterLink, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';
import { LoginResponse } from '../types/types';
import { filter } from 'rxjs';
import { Role } from '../types/types';

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
  role = Role;

  constructor(
    public authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;

      // cleanup when logged out
      if (!user) {
        this.dropdownOpen = false;
      }
    });

    const isPublicRoute = (url: string) => {
      const path = url.split('?')[0];
      return path === '/login' || path === '/oauth-callback' || path === '/oauth/callback';
    };

    // Validate only if the token exists AND we are not on the OAuth callback
    if (this.authService.isLoggedIn() && !isPublicRoute(this.router.url)) {
      this.authService.me().subscribe({
        next: () => {
          if (this.router.url.split('?')[0] === '/login') this.router.navigate(['/home']);
        },
        error: () => {
          this.authService.logout();
          this.router.navigate(['/login'], { queryParams: { oauth: 'session_expired' } });
        },
      });
    }

    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      const currentUrl = this.router.url;
      const path = currentUrl.split('?')[0];
      const isLoggedIn = this.authService.isLoggedIn();

      if (isLoggedIn && path === '/login') {
        this.router.navigate(['/home']);
        return;
      }

      // IMPORTANT: oauth-callback is public
      if (!isLoggedIn && !isPublicRoute(currentUrl)) {
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

  // Close the dropdown when you click outside it
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.dropdownOpen = false;
    }
  }
}
