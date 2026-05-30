import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-no-school-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './no-school-page.html',
  styleUrl: './no-school-page.css',
})
export class NoSchoolPage {
  router = inject(Router)
  authService = inject(AuthService)

  logoutAndRedirectToLogin() {
    this.authService.logout();
    this.router.navigate(["/login"]);
  }
}
