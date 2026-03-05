import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePassword {
  passwordForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private authService: AuthService,
    private router: Router,
  ) {
    this.passwordForm = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    });
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.errorMessage = 'Vul alle velden correct in';
      return;
    }

    const { oldPassword, newPassword, confirmPassword } = this.passwordForm.value;

    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Nieuwe wachtwoorden komen niet overeen';
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.email) {
      this.errorMessage = 'Geen gebruiker ingelogd';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http
      .post(
        `${environment.apiUrl}/user/change-password`,
        {
          email: currentUser.email,
          oldPassword,
          newPassword,
        },
        { responseType: 'text' },
      )
      .subscribe({
        next: () => {
          this.successMessage = 'Wachtwoord succesvol gewijzigd!';
          this.passwordForm.reset();
          this.loading = false;

          setTimeout(() => {
            this.router.navigate(['/profile']);
          }, 2000);
        },
        error: (err) => {
          console.error('Password change failed', err);
          this.errorMessage = err.error || 'Kon wachtwoord niet wijzigen';
          this.loading = false;
        },
      });
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
