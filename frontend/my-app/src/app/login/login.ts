import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  showAdminForm = false;
  adminEmail = '';
  adminPassword = '';
  adminLoginError: string | null = null;
  adminLoginLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.authService.me().subscribe({
        next: (user) => this.redirectAfterLogin(user),
        error: () => this.authService.logout(),
      });
    }
  }

  loginWithSmartschool() {
    window.location.href = this.authService.getOAuthLoginUrl();
  }

  loginWithCredentials(): void {
    if (!this.adminEmail || !this.adminPassword || this.adminLoginLoading) return;
    this.adminLoginLoading = true;
    this.adminLoginError = null;

    this.authService.login(this.adminEmail, this.adminPassword).subscribe({
      next: () => {
        this.authService.me().subscribe({
          next: (user) => {
            this.adminLoginLoading = false;
            this.redirectAfterLogin(user);
          },
          error: () => {
            this.adminLoginLoading = false;
            this.adminLoginError = 'Aanmelden mislukt.';
          },
        });
      },
      error: () => {
        this.adminLoginLoading = false;
        this.adminLoginError = 'Onjuist e-mailadres of wachtwoord.';
      },
    });
  }

  private redirectAfterLogin(user: { schoolId?: number | null }): void {
    if (user.schoolId === null) {
      this.router.navigate(['/setup']);
    } else {
      this.router.navigate(['/home']);
    }
  }

  // Dev login helpers
  loginAsAdmin() {
    this.authService.setToken('DEV_ADMIN_TOKEN');
    this.authService.setCurrentUser({ name: 'Admin', role: 'ADMIN', email: 'admin@gsmartlib.tech' });
    this.router.navigate(['/home']);
  }

  loginAsStudent() {
    this.authService.setToken('DEV_STUDENT_TOKEN');
    this.authService.setCurrentUser({ name: 'Test', role: 'LEERLING', email: 'leerling.dev@gsmartlib.tech' } as any);
    this.router.navigate(['/home']);
  }

  loginAsTeacher() {
    this.authService.setToken('DEV_TEACHER_TOKEN');
    this.authService.setCurrentUser({ name: 'Test', role: 'LEERKRACHT', email: 'leerkracht.dev@gsmartlib.tech' } as any);
    this.router.navigate(['/home']);
  }

  loginAsLibraryAdministrator() {
    this.authService.setToken('DEV_BIBLIOTHEEKBEHEERDER_TOKEN');
    this.authService.setCurrentUser({ name: 'Test', role: 'BIBLIOTHEEKBEHEERDER', email: 'bibliotheekbeheerder.dev@gsmartlib.tech' } as any);
    this.router.navigate(['/home']);
  }
}
