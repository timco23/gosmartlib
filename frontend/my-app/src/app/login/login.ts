import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { UserService } from '../services/user.service';

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

  loginAsStudent() {
    this.authService.setToken('DEV_STUDENT_TOKEN');
    this.authService.setCurrentUser({
      name: 'Test',
      first_name: 'Leerling',
      role: 'LEERLING',
      email: 'leerling.dev@gsmartlib.tech',
    } as any);
    this.router.navigate(['/home']);
  }

  loginAsTeacher() {
    this.authService.setToken('DEV_TEACHER_TOKEN');
    this.authService.setCurrentUser({
      name: 'Test',
      first_name: 'Leerkracht',
      role: 'LEERKRACHT',
      email: 'leerkracht.dev@gsmartlib.tech',
    } as any);
    this.router.navigate(['/home']);
  }

  loginAsLibraryAdministrator() {
    this.authService.setToken('DEV_BIBLIOTHEEKBEHEERDER_TOKEN');
    this.authService.setCurrentUser({
      name: 'Test',
      first_name: 'Bibliotheekbeheerder',
      role: 'BIBLIOTHEEKBEHEERDER',
      email: 'bibliotheekbeheerder.dev@gsmartlib.tech',
    } as any);
    this.router.navigate(['/home']);
  }
}
