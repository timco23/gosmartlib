import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { ConfirmDialogAction, Lending } from '../../../types/types';
import { LendingService } from '../../services/lending.service';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { PopUpComponent } from '../../pop-up/pop-up.component';

@Component({
  selector: 'app-lending-management',
  imports: [RouterLink, PopUpComponent],
  templateUrl: './lending-management.component.html',
  styleUrl: './lending-management.component.css',
})
export class LendingManagementComponent {
  lendingService: LendingService;
  public lendings: Lending[] = [];
  cdr = inject(ChangeDetectorRef);
  bookApi = inject(BookService);

  currentLending: Lending | null = null;
  extendConfirmation: boolean = false;
  extendConfirmationMessage: string = '';
  extendError: boolean = false;

  popUpActions: ConfirmDialogAction[] = [
    { label: 'Annuleren', type: 'cancel', value: 'cancel' },
    { label: 'Verlengen', type: 'primary', value: 'confirm' }
  ];

  private failedCoverIds = new Set<number>();

  constructor(lendingservice: LendingService) {
    this.lendingService = lendingservice;
  }

  public ngOnInit(): void {
    this.lendingService.getLendings().subscribe((data: Lending[]) => {
      this.lendings = data
        .filter((l) => !l.returned)
        .sort((a, b) => new Date(a.returnDate).getTime() - new Date(b.returnDate).getTime());
      this.cdr.detectChanges();
    });
  }

  onExtend(lending: Lending): void {
    if (lending.extended) {
      this.extendError = true;
      return;
    }

    this.currentLending = lending;

    this.extendConfirmation = true;
    this.extendConfirmationMessage = `Bent u zeker dat u de lening van "${lending.bookTitle}" wilt verlengen?`;
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
