import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SchoolService } from '../services/school.service';

@Component({
  selector: 'app-school-management',
  imports: [ReactiveFormsModule],
  standalone: true,
  templateUrl: './school-management.component.html',
  styleUrl: './school-management.component.css'
})
export class SchoolManagementComponent {
  schoolForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService
  ) {
    this.schoolForm = this.fb.group({
      name: [''],
      address: [''],
      lendingPeriodDays: [0],
      extendPeriodDays: [0],
      maxExtends: [0],
      maxLendsStudent: [0],
      maxLendTeacher: [0],
      lenderNotificationPeriod: [0],
      lenderLateNotificationPeriod: [0],
      librarianNotificationNotReturned: [0],
      librarianRequestNotification: [0],
      externalVisibility: [false],
      externalLending: [false],
      reviews: [false],
      reviewModeration: [false]
    });
  }

  onSubmit() {
    const school: School = this.schoolForm.value;

    this.schoolService.create(school)
      .subscribe({
        next: (response) => {
          console.log("School opgeslagen:", response);
          this.schoolForm.reset();
        },
        error: (err) => {
          console.error("Fout bij opslaan:", err);
        }
      });
  }
}

export interface School {
  id?: number;
  name: string;
  address: string;
  lendingPeriodDays: number;
  extendPeriodDays: number;
  maxExtends: number;
  maxLendsStudent: number;
  maxLendTeacher: number;
  lenderNotificationPeriod: number;
  lenderLateNotificationPeriod: number;
  librarianNotificationNotReturned: number;
  librarianRequestNotification: number;
  externalVisibility: boolean;
  externalLending: boolean;
  reviews: boolean;
  reviewModeration: boolean;
}