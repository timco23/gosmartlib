import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
  editingSchool: School | null = null;
  schools: School[] = [];

  showDeleteConfirm: boolean = false;

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef
  ) {
    this.schoolForm = this.fb.group({
      name: ['', [Validators.required]],
      address: ['', [Validators.required]],
      lendingPeriodDays: [0, [Validators.required, Validators.min(0)]],
      extendPeriodDays: [0, [Validators.required, Validators.min(0)]],
      maxExtends: [0, [Validators.required, Validators.min(0)]],
      maxLendsStudent: [0, [Validators.required, Validators.min(0)]],
      maxLendTeacher: [0, [Validators.required, Validators.min(0)]],
      lenderNotificationPeriod: [0, [Validators.required, Validators.min(0)]],
      lenderLateNotificationPeriod: [0, [Validators.required, Validators.min(0)]],
      librarianNotificationNotReturned: [0, [Validators.required, Validators.min(0)]],
      librarianRequestNotification: [false],
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

  selectSchool(school: School) {
    this.editingSchool = school;
    this.schoolForm.patchValue(school);
  }

  cancelForm() {
    this.addingSchool = false;
    this.editingSchool = null;
    this.schoolForm.reset();
  }

  onSubmit() {
    if (this.schoolForm.invalid) return;
    if (this.editingSchool) {
      const updated: School = { ...this.editingSchool, ...this.schoolForm.value };
      this.schoolService.update(updated).subscribe({
        next: () => {
          this.cancelForm();
          this.loadSchools();
        },
        error: (err) => console.error('error:', err)
      });
    } else {
      const school: School = this.schoolForm.value;
      this.schoolService.create(school).subscribe({
        next: () => {
          this.cancelForm();
          this.loadSchools();
        },
        error: (err) => console.error('error:', err)
      });
    }
  }

  deleteSchool(school: School) {
    if (school.id) {
      this.schoolService.delete(school).subscribe({
        next: () => this.loadSchools(),
        error: (err) => console.error('error:', err)
      });
    }
  }

  confirmDelete() {
    this.showDeleteConfirm = true;
  }

  confirmDeleteSchool() {
    if (this.editingSchool) {
      this.deleteSchool(this.editingSchool);
      this.showDeleteConfirm = false;
      this.cancelForm();
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
  librarianRequestNotification: boolean;
  externalVisibility: boolean;
  externalLending: boolean;
  reviews: boolean;
  reviewModeration: boolean;
}