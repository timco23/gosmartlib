import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SchoolService } from '../../services/school.service';
import { Campus, School, WizardStep } from '../../../types/types';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';

@Component({
  selector: 'app-school-wizard',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule, PopUpComponent],
  templateUrl: './school-wizard.html',
  styleUrl: './school-wizard.css',
})
export class SchoolWizard {
  @Output() schoolCreated = new EventEmitter<School>();
  @Output() cancelled = new EventEmitter<void>();

  currentStep = 0;
  campusInput = '';
  campuses: Campus[] = [];
  showCancelConfirm = false;
  isSubmitting = false;
  submitError = false;

  readonly steps: WizardStep[] = [
    { title: 'Schoolgegevens',      fields: ['name', 'address'] },
    { title: 'Ontleeninstellingen', fields: ['lendingPeriodDays', 'extendPeriodDays', 'maxExtends', 'maxLendsStudent', 'maxLendTeacher'] },
    { title: 'Meldingen',           fields: ['lenderNotificationPeriod', 'lenderLateNotificationPeriod', 'librarianNotificationNotReturned'] },
    { title: 'Extra opties',        fields: ['reviews', 'reviewModeration'] },
    { title: 'Dashboard',           fields: ['dashboardShowBookOfMonth', 'dashboardShowLastBorrowed', 'dashboardShowAnnouncement', 'dashboardAnnouncement'] },
    { title: 'Samenvatting',        fields: [] },
  ];

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private schoolService: SchoolService,
    private cdr: ChangeDetectorRef,
  ) {
    this.form = this.fb.group({
      name:                            ['', [Validators.required, Validators.maxLength(100)]],
      address:                         ['', [Validators.required, Validators.maxLength(200)]],
      lendingPeriodDays:               ['',  [Validators.required, Validators.min(0)]],
      extendPeriodDays:                ['',  [Validators.required, Validators.min(0)]],
      maxExtends:                      ['',  [Validators.required, Validators.min(0)]],
      maxLendsStudent:                 ['',  [Validators.required, Validators.min(0)]],
      maxLendTeacher:                  ['',  [Validators.required, Validators.min(0)]],
      lenderNotificationPeriod:        ['',  [Validators.required, Validators.min(0)]],
      lenderLateNotificationPeriod:    ['',  [Validators.required, Validators.min(0)]],
      librarianNotificationNotReturned:['',  [Validators.required, Validators.min(0)]],
      reviews:                         [false],
      reviewModeration:                [false],
      dashboardShowBookOfMonth:        [true],
      dashboardShowLastBorrowed:       [true],
      dashboardShowAnnouncement:       [false],
      dashboardAnnouncement:           ['', Validators.maxLength(500)],
    });
  }

  get isLastStep(): boolean { return this.currentStep === this.steps.length - 1; }
  get isFirstStep(): boolean { return this.currentStep === 0; }
  get hasUnsavedData(): boolean { return this.form.dirty || this.campuses.length > 0; }
  get campusNames(): string { return this.campuses.map(c => c.name).join(', '); }

  goNext(): void {
    const fields = this.steps[this.currentStep].fields;
    const invalid = fields.some(f => this.form.get(f)?.invalid);
    if (invalid) {
      fields.forEach(f => this.form.get(f)?.markAsTouched());
      return;
    }
    this.currentStep++;
  }

  goPrevious(): void {
    if (this.currentStep > 0) this.currentStep--;
  }

  goToStep(index: number): void {
    if (index < this.currentStep) this.currentStep = index;
  }

  addCampus(): void {
    if (this.campusInput.trim()) {
      this.campuses.push({ name: this.campusInput.trim() });
      this.campusInput = '';
      this.form.markAsDirty();
    }
  }

  removeCampus(index: number): void {
    this.campuses.splice(index, 1);
    this.form.markAsDirty();
  }

  onSubmit(): void {
    if (this.form.invalid || this.isSubmitting) return;
    this.isSubmitting = true;
    this.submitError = false;
    const schoolData = { ...this.form.value, campuses: this.campuses };
    this.schoolService.create(schoolData).subscribe({
      next: (saved) => {
        this.isSubmitting = false;
        this.schoolCreated.emit(saved);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('error:', err);
        this.isSubmitting = false;
        this.submitError = true;
        this.cdr.detectChanges();
      },
    });
  }

  requestCancel(): void {
    if (this.hasUnsavedData) {
      this.showCancelConfirm = true;
    } else {
      this.cancelled.emit();
    }
  }

  onCancelConfirm(value: string): void {
    this.showCancelConfirm = false;
    if (value === 'confirm') this.cancelled.emit();
  }

  yesNo(value: boolean): string { return value ? 'Ja' : 'Nee'; }
}
