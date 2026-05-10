import { ChangeDetectorRef, Component, inject, Input } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LendingService } from '../services/lending.service';
import { Lending, LoginResponse } from '../../types/types';
import { AuthService } from '../services/auth.service';
import { BookService } from '../services/book-service';
import { filter, switchMap } from 'rxjs';

@Component({
  selector: 'app-lending-history',
  imports: [RouterLink],
  templateUrl: './lending-history.component.html',
  styleUrl: './lending-history.component.css',
})
export class LendingHistoryComponent {
  api: LendingService = inject(LendingService);
  lendings: Lending[] = [];
  currentUser: LoginResponse | null = null;
  cdr = inject(ChangeDetectorRef);
  bookApi: BookService = inject(BookService);
  failedCoverIds = new Set<number>();

  @Input() userId?: number;

  currentPage = 0;
  pageSize = 10;

  get totalPages(): number {
    return Math.ceil(this.lendings.length / this.pageSize);
  }

  get visibleLendings(): Lending[] {
    const start = this.currentPage * this.pageSize;
    return this.lendings.slice(start, start + this.pageSize);
  }

  constructor(private authService: AuthService, private route: ActivatedRoute) {}
  
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const userId = params['userId'];
      if (userId) {
        this.api.getLendingsByUserId(Number(userId)).subscribe((data: Lending[]) => {
          this.lendings = (data ?? []).filter((l) => l.returned || !!l.dateReturned);
          this.currentPage = 0;
          this.cdr.detectChanges();
        });
      } else {
        this.authService.currentUser$
          .pipe(
            filter((user) => !!user?.email),
            switchMap((user) => {
              this.currentUser = user;
              return this.api.getLendingsByUser(this.currentUser!.email);
            }),
          )
          .subscribe((data: Lending[]) => {
            this.lendings = (data ?? []).filter((l) => l.returned || !!l.dateReturned);
            this.currentPage = 0;
            this.cdr.detectChanges();
          });
      }
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.cdr.detectChanges();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.cdr.detectChanges();
    }
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
