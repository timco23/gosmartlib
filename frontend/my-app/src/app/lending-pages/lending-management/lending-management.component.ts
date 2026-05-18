import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { ConfirmDialogAction, Lending, School } from '../../../types/types';
import { LendingService } from '../../services/lending.service';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { SchoolService } from '../../services/school.service';

@Component({
  selector: 'app-lending-management',
  imports: [RouterLink, PopUpComponent, FormsModule],
  templateUrl: './lending-management.component.html',
  styleUrl: './lending-management.component.css',
})
export class LendingManagementComponent {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private schoolService = inject(SchoolService);

  lendingService: LendingService;
  public lendings: Lending[] = [];
  cdr = inject(ChangeDetectorRef);
  bookApi = inject(BookService);

  private currentSchool: School | null = null;

  currentLending: Lending | null = null;
  extendConfirmation: boolean = false;
  extendConfirmationMessage: string = '';
  extendError: boolean = false;
  extendErrorMessage: string = '';

  popUpActions: ConfirmDialogAction[] = [
    { label: 'Annuleren', type: 'cancel', value: 'cancel' },
    { label: 'Verlengen', type: 'primary', value: 'confirm' }
  ];

  searchQuery: string = '';

  get filteredLendings(): Lending[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.lendings;
    return this.lendings.filter(l =>
      l.bookTitle?.toLowerCase().includes(q) ||
      l.bookAuthor?.toLowerCase().includes(q) ||
      l.borrowerName?.toLowerCase().includes(q)
    );
  }

  private failedCoverIds = new Set<number>();

  constructor(lendingservice: LendingService) {
    this.lendingService = lendingservice;
  }

    public ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      if (!user?.email) return;
      this.userService.getUser(user.email).subscribe({
        next: (data) => {
          if (!data.schoolName) return;
          this.schoolService.getByName(data.schoolName).subscribe({
            next: (school) => {
              this.currentSchool = school;
              this.lendingService.getLendingsBySchool(school.id!).subscribe({
                next: (data: Lending[]) => {
                  this.lendings = data
                    .filter((l) => !l.returned)
                    .sort((a, b) => new Date(a.returnDate).getTime() - new Date(b.returnDate).getTime());
                  this.cdr.detectChanges();
                }
              });
            }
          });
        }
      });
    });
  }

  canExtend(lending: Lending): boolean {
    if (!this.currentSchool) return false;
    return (lending.extendCount ?? 0) < this.currentSchool.maxExtends;
  }

  onExtend(lending: Lending): void {
    if (!this.canExtend(lending)) {
      const max = this.currentSchool?.maxExtends ?? 0;
      this.extendErrorMessage = max === 1
        ? `De lening van "${lending.bookTitle}" kan slechts 1 keer verlengd worden en is al verlengd.`
        : `De lening van "${lending.bookTitle}" kan maximaal ${max} keer verlengd worden en heeft dit maximum bereikt.`;
      this.extendError = true;
      return;
    }

    this.currentLending = lending;
    const days = this.currentSchool?.extendPeriodDays ?? 0;
    this.extendConfirmation = true;
    this.extendConfirmationMessage =
      `Bent u zeker dat u de lening van "${lending.bookTitle}" wilt verlengen met ${days} dagen?`;
  }

  coverSrcFromLending(lending: Lending): string | null {
    const id = Number(lending.itemId);
    if (!Number.isFinite(id) || id <= 0 || this.failedCoverIds.has(id)) return null;
    return this.bookApi.getBookCoverUrl(id);
  }

  onCoverError(_event: Event, lending: Lending): void {
    const id = Number(lending.itemId);
    if (Number.isFinite(id) && id > 0) {
      this.failedCoverIds.add(id);
      this.cdr.detectChanges();
    }
  }

  closeExtendErrorPopup(): void {
    this.extendError = false;
    this.extendErrorMessage = '';
  }

  onExtendDialogAction(value: string): void {
    this.extendConfirmation = false;
    if (value !== 'confirm' || !this.currentLending) return;

    const lending = this.currentLending;

    this.lendingService.extendLending(lending.id).subscribe({
      next: (updatedLending) => {
        const index = this.lendings.findIndex((l) => l.id === lending.id);
        if (index !== -1) {
          this.lendings[index] = updatedLending;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Fout bij verlengen van lening:', err);
        alert('Er is een fout opgetreden bij het verlengen van de lening. Probeer het later opnieuw.');
      },
    });
  }
}
