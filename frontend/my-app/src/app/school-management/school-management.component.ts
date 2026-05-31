import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SchoolService } from '../services/school.service';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';
import { CommonModule, Location } from '@angular/common';
import { Campus, School, UserProfile } from '../../types/types';
import { FormsModule } from '@angular/forms';
import { PopUpComponent } from "../widgets/pop-up/pop-up.component";
import { SchoolWizard } from './school-wizard/school-wizard';

@Component({
  selector: 'app-school-management',
  imports: [ReactiveFormsModule, CommonModule, FormsModule, PopUpComponent, SchoolWizard],
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
  campusesModified: boolean = false;
  saveStatus: 'success' | 'error' | null = null;
  showBackConfirm: boolean = false;
  private saveStatusTimer: any = null;
  schoolFilter: string = '';

  get filteredSchools(): School[] {
    const q = this.schoolFilter.toLowerCase().trim();
    if (!q) return this.schools;
    return this.schools.filter(s => s.name?.toLowerCase().includes(q));
  }

  currentPage = 0;
  readonly pageSize = 10;

  get totalPages(): number {
    return Math.ceil(this.filteredSchools.length / this.pageSize);
  }

  get pagedSchools(): School[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredSchools.slice(start, start + this.pageSize);
  }

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private userService: UserService,
    private location: Location
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
      dashboardShowBookOfMonth: [true],
      dashboardShowLastBorrowed: [true],
      dashboardShowAnnouncement: [false],
      dashboardAnnouncement: ['', Validators.maxLength(500)],
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


  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) this.currentPage++;
  }

  previousPage(): void {
    if (this.currentPage > 0) this.currentPage--;
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
      this.schoolForm.markAsPristine();
      this.campusesModified = false;
      this.cdr.detectChanges();
    });
  }

  switchSchool(school: School) {
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
    this.schoolForm.markAsPristine();
    this.campusesModified = false;
  }

  cancelForm() {
    if (this.hasUnsavedChanges) {
      this.showBackConfirm = true;
      return;
    }
    this.navigateBack();
  }

  onBackConfirmAction(value: string) {
    this.showBackConfirm = false;
    if (value === 'confirm') this.navigateBack();
  }

  private navigateBack() {
    if (this.isLibrarian) {
      this.location.back();
      return;
    }
    this.addingSchool = false;
    this.editingSchool = null;
    this.schoolForm.reset();
    this.campuses = [];
    this.campusInput = '';
    this.campusesModified = false;
  }

  onDeleteDialogAction(value: string) {
    if (value === 'confirm') this.confirmDeleteSchool();
    else this.showDeleteConfirm = false;
  }

  addCampus() {
    if (this.campusInput.trim()) {
      this.campuses.push({ name: this.campusInput.trim() });
      this.campusInput = '';
      this.campusesModified = true;
    }
  }

  removeCampus(index: number) {
    this.campuses.splice(index, 1);
    this.campusesModified = true;
  }

  get hasUnsavedChanges(): boolean {
    return this.schoolForm.dirty || this.campusesModified;
  }

  onSubmit() {
    if (this.schoolForm.invalid) return;

    const schoolData = { ...this.schoolForm.value, campuses: this.campuses };

    if (this.isLibrarian && this.editingSchool) {
      const updated: School = { ...this.editingSchool, ...schoolData };
      this.schoolService.updateCurrentSchoolSettings(updated).subscribe({
        next: () => {
          this.showSaveStatus('success');
          this.loadCurrentSchoolForLibrarian();
        },
        error: (err) => {
          console.error('error:', err);
          this.showSaveStatus('error');
        }
      });
    } else if (this.editingSchool) {
      const updated: School = { ...this.editingSchool, ...schoolData };
      this.schoolService.update(updated).subscribe({
        next: (saved) => {
          this.editingSchool = saved;
          this.schoolForm.patchValue(saved);
          this.campuses = saved.campuses ? [...saved.campuses] : [];
          this.schoolForm.markAsPristine();
          this.campusesModified = false;
          this.loadSchools();
          this.showSaveStatus('success');
        },
        error: (err) => {
          console.error('error:', err);
          this.showSaveStatus('error');
        }
      });
    }
  }

  onWizardSchoolCreated(_saved: School): void {
    this.addingSchool = false;
    this.loadSchools();
    this.cdr.detectChanges();
  }

  onWizardCancelled(): void {
    this.addingSchool = false;
    this.cdr.detectChanges();
  }

  private showSaveStatus(status: 'success' | 'error') {
    if (this.saveStatusTimer) clearTimeout(this.saveStatusTimer);
    this.saveStatus = status;
    this.cdr.detectChanges();
    this.saveStatusTimer = setTimeout(() => {
      this.saveStatus = null;
      this.cdr.detectChanges();
    }, 3000);
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