import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { UserService } from '../services/user.service';
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
    private userService: UserService
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.authService.me().subscribe({
        next: (data) => {
        this.userService.getUser(data.email).subscribe({
          next: (fullData) => {
            if (fullData.schoolId) {
              this.router.navigate(['/home'])
            }
            else {
              this.router.navigate(['/noschool'])
            }
          },
          error: () => this.authService.logout()
        })
      },
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
  
}
