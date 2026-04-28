import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LendingService } from '../services/lending.service';
import { Lending, LoginResponse } from '../../types/types';
import { AuthService } from '../services/auth.service';
import { BookService } from '../services/book-service';
import { filter, switchMap } from 'rxjs';

@Component({
  selector: 'app-lending-history',
  imports: [RouterLink],
  templateUrl: './lending-history.component.html',
  styleUrl: './lending-history.component.css'
})
export class LendingHistoryComponent {
  api: LendingService = inject(LendingService);
  lendings: Lending[] = [];
  currentUser: LoginResponse | null = null;
  cdr = inject(ChangeDetectorRef);
  bookApi: BookService = inject(BookService); 
  failedCoverIds = new Set<number>();

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(
        filter((user) => !!user?.email),
        switchMap((user) => {
          this.currentUser = user;
          return this.api.getLendingsByUserId(this.currentUser!.email);
        })
      )
      .subscribe((data: Lending[]) => {
        this.lendings = data;
        this.cdr.detectChanges();
      });
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
}
