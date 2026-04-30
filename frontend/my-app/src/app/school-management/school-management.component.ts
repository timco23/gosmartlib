import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SchoolService } from '../services/school.service';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { CommonModule } from '@angular/common';
import { Campus, School, UserProfile } from '../../types/types';
import { FormsModule } from '@angular/forms';
import { PopUpComponent } from "../pop-up/pop-up.component";

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
  schools: School[] = [];
  showDeleteConfirm: boolean = false;
  campusInput: string = '';
  campuses: Campus[] = [];
  currentUser: UserProfile | null = null;
  showSwitchSchoolConfirm: boolean = false;
  schoolToSwitch: School | null = null;
  isLibrarian: boolean = false;

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private userService: UserService
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
    const loginUser = this.authService.getCurrentUser();
    this.isLibrarian = loginUser?.role === 'BIBLIOTHEEKBEHEERDER';

    this.loadCurrentUser();

    if (this.isLibrarian) {
      this.loadCurrentSchoolForLibrarian();
    } else {
      this.loadSchools();
    }
  }

  loadCurrentUser() {
    const loginUser = this.authService.getCurrentUser();
    if (loginUser?.email) {
      this.userService.getUser(loginUser.email).subscribe(profile => {
        this.currentUser = profile;
        this.cdr.detectChanges();
      });
    }
  }

  loadCurrentSchoolForLibrarian() {
    this.schoolService.getCurrentSchool().subscribe(school => {
      this.editingSchool = school;
      this.schoolForm.patchValue(school);
      this.campuses = school.campuses ? [...school.campuses] : [];
      this.schoolForm.get('name')?.disable();
      this.schoolForm.get('address')?.disable();
      this.cdr.detectChanges();
    });
  }

  initiateSwitchSchool(school: School) {
    this.schoolToSwitch = school;
    this.showSwitchSchoolConfirm = true;
  }

  processSchoolSwitchDialog(value: string) {
    const email = this.authService.getCurrentUser()?.email;
    if (value === 'confirm' && this.schoolToSwitch?.id && email) {
      this.userService.changeSchool(email, this.schoolToSwitch.id).subscribe({
        next: (updatedUser) => {
          this.currentUser = updatedUser;
          this.showSwitchSchoolConfirm = false;
          this.schoolToSwitch = null;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('error:', err)
      });
    } else {
      this.showSwitchSchoolConfirm = false;
      this.schoolToSwitch = null;
    }
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
    this.campuses = school.campuses ? [...school.campuses] : [];
  }

  cancelForm() {
    if (this.isLibrarian) {
      this.loadCurrentSchoolForLibrarian();
      return;
    }
    this.addingSchool = false;
    this.editingSchool = null;
    this.schoolForm.reset();
    this.campuses = [];
    this.campusInput = '';
  }

  onDeleteDialogAction(value: string) {
    if (value === 'confirm') this.confirmDeleteSchool();
    else this.showDeleteConfirm = false;
  }

  addCampus() {
    if (this.campusInput.trim()) {
      this.campuses.push({ name: this.campusInput.trim() });
      this.campusInput = '';
    }
  }

  removeCampus(index: number) {
    this.campuses.splice(index, 1);
  }

  onSubmit() {
    if (this.schoolForm.invalid) return;

    const schoolData = { ...this.schoolForm.value, campuses: this.campuses };

    if (this.isLibrarian && this.editingSchool) {
      const updated: School = { ...this.editingSchool, ...schoolData };
      this.schoolService.updateCurrentSchoolSettings(updated).subscribe({
        next: () => this.loadCurrentSchoolForLibrarian(),
        error: (err) => console.error('error:', err)
      });
    } else if (this.editingSchool) {
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
