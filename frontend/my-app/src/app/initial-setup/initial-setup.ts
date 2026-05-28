import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { SchoolWizard } from '../school-management/school-wizard/school-wizard';
import { School } from '../../types/types';

type Phase = 'welcome' | 'password' | 'school' | 'done';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('newPassword')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { mismatch: true } : null;
}

@Component({
  selector: 'app-initial-setup',
  standalone: true,
  imports: [CommonModule, SchoolWizard, ReactiveFormsModule],
  templateUrl: './initial-setup.html',
  styleUrl: './initial-setup.css',
})
export class InitialSetupComponent {
  phase: Phase = 'welcome';
  isLinking = false;
  linkError = false;
  passwordForm: FormGroup;
  passwordLoading = false;
  passwordError: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder,
  ) {
    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', Validators.required],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordsMatch },
    );
  }

  startSetup(): void {
    this.phase = 'password';
    this.cdr.detectChanges();
  }

  submitPassword(): void {
    if (this.passwordForm.invalid || this.passwordLoading) return;
    this.passwordLoading = true;
    this.passwordError = null;

    const { currentPassword, newPassword } = this.passwordForm.value;
    this.authService.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.phase = 'school';
        this.passwordLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.passwordLoading = false;
        this.passwordError = 'Wachtwoord wijzigen mislukt. Controleer uw huidig wachtwoord.';
        this.cdr.detectChanges();
      },
    });
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
