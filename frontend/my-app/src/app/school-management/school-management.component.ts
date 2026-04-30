import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SchoolService } from '../services/school.service';
import { CommonModule } from '@angular/common';
import { Campus, School } from '../../types/types';
import { FormsModule } from '@angular/forms';
import { PopUpComponent } from "../widgets/pop-up/pop-up.component";

@Component({
  selector: 'app-school-management',
  imports: [ReactiveFormsModule, CommonModule, FormsModule, PopUpComponent],
  standalone: true,
  templateUrl: './school-management.component.html',
  styleUrl: './school-management.component.css'
})

export class SchoolManagementComponent {
  schoolForm: FormGroup;
  addingSchool: boolean = false;
  editingSchool: School | null = null;
  viewingSchool: School | null = null;
  schools: School[] = [];
  showDeleteConfirm: boolean = false;
  campusInput: string = '';
  campuses: Campus[] = [];

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef
  ) {
    this.schoolForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      address: ['', [Validators.required, Validators.maxLength(200)]],
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

  viewSchool(school: School) {
    this.viewingSchool = school;
  }

  closeDetails() {
    this.viewingSchool = null;
  }

  loadSchools() {
    this.schoolService.getAll().subscribe(data => {
      this.schools = [...data];
      this.cdr.detectChanges();
    });
  }

  selectSchool(school: School) {
    this.viewingSchool = null;
    this.editingSchool = school;
    this.schoolForm.patchValue(school);
    this.campuses = school.campuses ? [...school.campuses] : [];
  }

  cancelForm() {
    this.addingSchool = false;
    this.editingSchool = null;
    this.viewingSchool = null;
    this.schoolForm.reset();
    this.campuses = [];
    this.campusInput = '';
  }

  onDeleteDialogAction(value: string) {
    if (value === 'confirm') this.confirmDeleteSchool();
    else this.showDeleteConfirm = false;
  }

  // Campus toevoegen
  addCampus() {
    if (this.campusInput.trim()) {
      this.campuses.push({ name: this.campusInput.trim() });
      this.campusInput = '';
    }
  }

  // Campus verwijderen
  removeCampus(index: number) {
    this.campuses.splice(index, 1);
  }

  onSubmit() {
    if (this.schoolForm.invalid) return;

    const schoolData = { ...this.schoolForm.value, campuses: this.campuses };

    if (this.editingSchool) {
      const updated: School = { ...this.editingSchool, ...schoolData };
      this.schoolService.update(updated).subscribe({
        next: () => {
          this.cancelForm();
          this.loadSchools();
        },
        error: (err) => console.error('error:', err)
      });
    } else {
      this.schoolService.create(schoolData).subscribe({
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