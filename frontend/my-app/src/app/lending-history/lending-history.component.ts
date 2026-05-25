import { ChangeDetectorRef, Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LendingService } from '../services/lending.service';
import { Lending, LoginResponse } from '../../types/types';
import { AuthService } from '../services/auth.service';
import { BookService } from '../services/book-service';
import { filter, switchMap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-lending-history',
  imports: [RouterLink, FormsModule],
  templateUrl: './lending-history.component.html',
  styleUrl: './lending-history.component.css',
})
export class LendingHistoryComponent implements OnChanges {
  api: LendingService = inject(LendingService);
  lendings: Lending[] = [];
  currentUser: LoginResponse | null = null;
  cdr = inject(ChangeDetectorRef);
  bookApi: BookService = inject(BookService);
  userService: UserService = inject(UserService);
  failedCoverIds = new Set<number>();
  isShowingOtherUser: Boolean = false;
  otherUser: string = "";
  userEmail: string = "";
  userName: string = "";

  searchQuery: string = '';

  currentPage = 0;
  pageSize = 10;
  
  @Input() preloadedLendings?: Lending[];

  get totalPages(): number {
    return Math.ceil(this.lendings.length / this.pageSize);
  }

  get visibleLendings(): Lending[] {
    const start = this.currentPage * this.pageSize;
    return this.lendings.slice(start, start + this.pageSize);
  }

  constructor(private authService: AuthService, private route: ActivatedRoute) {}

  ngOnChanges(changes: SimpleChanges): void {
    if ('preloadedLendings' in changes && this.preloadedLendings !== undefined) {
      this.lendings = this.preloadedLendings;
      this.currentPage = 0;
      this.cdr.detectChanges();
    }
  }

  ngOnInit(): void {
    if (this.preloadedLendings !== undefined) return;

    this.route.queryParams.subscribe(params => {
      const userId = params['userId'];
      const userEmail = params['userEmail']
      if (userId != null) {
        this.isShowingOtherUser = true;
        this.userEmail = userEmail
        this.api.getLendingsByUserId(Number(userId)).subscribe((data: Lending[]) => {
          this.lendings = (data ?? []).filter((l) => l.returned || !!l.dateReturned);
          this.currentPage = 0;

            this.userService.getProfileByEmail(this.userEmail).subscribe({
              next: (user) => {
                this.userName = user.name;
                this.cdr.detectChanges();
              }
            });

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

  get filteredLendings(): Lending[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.visibleLendings;
    return this.visibleLendings.filter(l =>
      l.bookTitle?.toLowerCase().includes(q) ||
      l.bookAuthor?.toLowerCase().includes(q) ||
      l.borrowerName?.toLowerCase().includes(q)
    );
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
