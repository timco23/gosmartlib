import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SchoolService } from '../services/school.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-school-management',
  imports: [ReactiveFormsModule, CommonModule],
  standalone: true,
  templateUrl: './school-management.component.html',
  styleUrl: './school-management.component.css'
})
export class SchoolManagementComponent {
  schoolForm: FormGroup;

  addingSchool: boolean = false;
  schools: School[] = [];

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef
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

  ngOnInit(): void {
    this.loadSchools();
  }

  loadSchools() {
    this.schoolService.getAll().subscribe(data => {
      this.schools = [...data];
      this.cdr.detectChanges();
    });
  }

  onSubmit() {
    const school: School = this.schoolForm.value;

    this.schoolService.create(school)
      .subscribe({
        next: (response) => {
          this.schoolForm.reset();
          this.addingSchool = false;
          this.loadSchools();
        },
        error: (err) => {
          console.error("error:", err);
        }
      });
  }

  deleteSchool(school: School) {
    if (school.id) {
      this.schoolService.delete(school)
        .subscribe({
          next: (response) => {
            this.loadSchools();
          },
          error: (err) => {
            console.error("error:", err);
          }
        });
    }
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