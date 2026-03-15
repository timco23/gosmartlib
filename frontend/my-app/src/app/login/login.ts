import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';

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
    // Check of user al ingelogd is
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/home']);
    }
  }

  loginWithSmartschool() {
    // For OAuth
    alert('Smartschool OAuth komt eraan. Gebruik voorlopig Dev login.');
  }

  loginAsAdmin() {
    this.authService.setCurrentUser({
      name: 'Admin',
      role: 'ADMIN',
      email: 'admin@gsmartlib.tech',
    });
    this.router.navigate(['/home']);
  }

  loginAsStudent() {
    this.authService.setCurrentUser({
      name: 'John Wick',
      role: 'LEERLING',
      email: 's123456@gsmartlib.tech',
    });
    this.router.navigate(['/home']);
  }
}
