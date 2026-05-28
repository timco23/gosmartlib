import { CommonModule } from '@angular/common';
import { Component, OnInit, HostBinding } from '@angular/core';
import { RouterOutlet, Router, RouterLink, NavigationEnd } from '@angular/router';
import { AuthService } from './services/auth.service';
import { LoginResponse } from '../types/types';
import { filter } from 'rxjs';
import { Role } from '../types/types';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  currentUser: LoginResponse | null = null;
  isDarkMode = false;
  sidebarRight = false;
  mobileNavOpen = false;
  role = Role;
  hasSchool = false;

  @HostBinding('class.sidebar-right') get sidebarRightClass() {
    return this.sidebarRight;
  }

  get hideNav(): boolean {
    return this.router.url.split('?')[0] === '/setup';
  }

  constructor(
    public authService: AuthService,
    private router: Router,
    private userService: UserService,
  ) {}

  ngOnInit() {
    const savedTheme = localStorage.getItem('gsmart-theme');
    this.isDarkMode = savedTheme === 'dark';
    this.applyTheme();

    this.sidebarRight = localStorage.getItem('gsmart-sidebar') === 'right';
    this.applySidebarPosition();

    this.setBodyScrollLock(false);

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      this.authService.currentUser$.subscribe((user) => {
        this.currentUser = user;
      });
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
      this.mobileNavOpen = false;
      this.setBodyScrollLock(false);
      const currentUrl = this.router.url;
      const path = currentUrl.split('?')[0];
      const isLoggedIn = this.authService.isLoggedIn();

      if (isLoggedIn && path === '/login') {
        this.router.navigate(['/home']);
        return;
      }

      // oauth-callback is public
      if (!isLoggedIn && !isPublicRoute(currentUrl)) {
        this.router.navigate(['/login']);
      }
    });
  }

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('gsmart-theme', this.isDarkMode ? 'dark' : 'light');
  }

  toggleSidebarPosition() {
    this.sidebarRight = !this.sidebarRight;
    this.applySidebarPosition();
    localStorage.setItem('gsmart-sidebar', this.sidebarRight ? 'right' : 'left');
  }

  private applySidebarPosition() {
    document.documentElement.setAttribute('data-sidebar', this.sidebarRight ? 'right' : 'left');
  }

  private applyTheme() {
    document.documentElement.setAttribute('data-theme', this.isDarkMode ? 'dark' : 'light');
  }

  toggleMobileNav() {
    this.mobileNavOpen = !this.mobileNavOpen;
    this.setBodyScrollLock(this.mobileNavOpen);
  }

  closeMobileNav() {
    this.mobileNavOpen = false;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  private setBodyScrollLock(locked: boolean) {
    document.body.classList.toggle('no-scroll', locked);
  }
}
