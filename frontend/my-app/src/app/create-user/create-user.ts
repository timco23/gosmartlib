import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { SchoolService } from '../services/school.service';
import { School } from '../../types/types';

/**
 * Component for creating new student users.
 * Admin-only feature for adding students to the system.
 */
@Component({
  selector: 'app-create-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-user.html',
  styleUrl: './create-user.css',
})
export class CreateUser implements OnInit {
  userForm: FormGroup;
  schools: School[] = [];
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private schoolService: SchoolService,
    private router: Router,
  ) {
    this.userForm = this.fb.group({
      firstName: ['', [Validators.required]],
      name: ['', [Validators.required]],
      studentNumber: ['', [Validators.required, Validators.pattern(/^s\d{6}$/)]],
      className: ['', [Validators.required]],
      schoolId: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadSchools();
  }

  /**
   * Loads all schools for dropdown selection.
   */
  loadSchools(): void {
    this.schoolService.getAll().subscribe({
      next: (data) => {
        this.schools = data;
      },
      error: (err) => {
        console.error('Failed to load schools', err);
        this.errorMessage = 'Kon scholen niet laden';
      },
    });
  }

  /**
   * Submits the form to create a new student.
   * Auto-generates email and password.
   */
  onSubmit(): void {
    if (this.userForm.invalid) {
      this.errorMessage = 'Vul alle velden correct in';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.userService.createUser(this.userForm.value).subscribe({
      next: (response) => {
        this.successMessage = `Gebruiker aangemaakt! Email: ${response.email} | Wachtwoord: Welkom2024!`;
        this.userForm.reset();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to create user', err);
        this.errorMessage = err.error?.message || 'Kon gebruiker niet aanmaken';
        this.loading = false;
      },
    });
  }

  /**
   * Navigates back to home.
   */
  cancel(): void {
    this.router.navigate(['/home']);
  }
}
