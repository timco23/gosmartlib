import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Lending } from '../../../types/types';
import { LendingService } from '../../services/lending.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-lending-management',
  imports: [RouterLink],
  templateUrl: './lending-management.component.html',
  styleUrl: './lending-management.component.css'
})
export class LendingManagementComponent {
  lendingService: LendingService;
  public lendings: Lending[] = [];
  cdr = inject(ChangeDetectorRef);

  constructor(lendingservice: LendingService) {
    this.lendingService = lendingservice;
  }

  public ngOnInit(): void {
    this.lendingService.getLendings().subscribe((data: Lending[]) => {
      this.lendings = data
        .filter(l => !l.returned)
        .sort((a, b) => new Date(a.returnDate).getTime() - new Date(b.returnDate).getTime());
      this.cdr.detectChanges();
    });
  }

  onExtend(lending: Lending): void {
    if (lending.extended) {
      alert('Deze lening is al verlengd.');
      return;
    }

    const bevestigd = confirm(`Bent u zeker dat u de lening van "${lending.bookTitle}" voor ${lending.borrowerName} wilt verlengen?`);
    if (!bevestigd) return;

    this.lendingService.extendLending(lending.id).subscribe({
      next: (updatedLending) => {
        const index = this.lendings.findIndex(l => l.id === lending.id);
        if (index !== -1) {
          this.lendings[index] = updatedLending;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Fout bij verlengen van lening:', err);
        alert('Er is een fout opgetreden bij het verlengen van de lening. Probeer het later opnieuw.');
      }
    });
  }
}
