import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { SchoolWizard } from '../school-management/school-wizard/school-wizard';
import { ChangePasswordComponent } from '../change-password/change-password';
import { School } from '../../types/types';

type Phase = 'welcome' | 'password' | 'school' | 'done';

@Component({
  selector: 'app-initial-setup',
  standalone: true,
  imports: [CommonModule, SchoolWizard, ChangePasswordComponent],
  templateUrl: './initial-setup.html',
  styleUrl: './initial-setup.css',
})
export class InitialSetupComponent {
  phase: Phase = 'welcome';
  isLinking = false;
  linkError = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  startSetup(): void {
    this.phase = 'password';
    this.cdr.detectChanges();
  }

  onPasswordChanged(): void {
    this.phase = 'school';
    this.cdr.detectChanges();
  }

  onSchoolCreated(school: School): void {
    const adminEmail = this.authService.getCurrentUser()?.email;
    if (!adminEmail || !school.id) {
      this.phase = 'done';
      this.cdr.detectChanges();
      return;
    }

    this.isLinking = true;
    this.cdr.detectChanges();

    this.userService.changeSchool(adminEmail, school.id).subscribe({
      next: () => {
        this.authService.me().subscribe({
          next: () => {
            this.phase = 'done';
            this.isLinking = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.phase = 'done';
            this.isLinking = false;
            this.cdr.detectChanges();
          },
        });
      },
      error: () => {
        this.isLinking = false;
        this.linkError = true;
        this.cdr.detectChanges();
      },
    });
  }

  goToApp(): void {
    this.router.navigate(['/home']);
  }
}
