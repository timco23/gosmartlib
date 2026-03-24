import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.authService.me().subscribe({
        next: () => this.router.navigate(['/home']),
        error: () => this.authService.logout(),
      });
    }
  }

  loginWithSmartschool() {
    window.location.href = this.authService.getOAuthLoginUrl();
  }

  //For testing
  loginAsAdmin() {
    //Fake token
    this.authService.setToken('DEV_ADMIN_TOKEN');

    this.authService.setCurrentUser({
      name: 'Admin',
      role: 'ADMIN',
      email: 'admin@gsmartlib.tech',
    });
    this.router.navigate(['/home']);
  }
}
