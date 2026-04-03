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
}
