import { ChangeDetectorRef, Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Lending, LoginResponse, Role, UserProfile } from '../../types/types';
import { UserService } from '../services/user.service';
import { LendingService } from '../services/lending.service';
import { BookService } from '../services/book-service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit, OnDestroy {
  lendingService: LendingService = inject(LendingService);
  lendings: Lending[] = [];
  activeLendings: Lending[] = [];
  bookApi: BookService = inject(BookService);
  failedCoverIds = new Set<number>();

  profile: UserProfile | null = null;
  loading: boolean = true;
  error: string = '';
  currentUser: LoginResponse | null = null;

  isViewingOther: boolean = false;
  selectedRole: string = '';
  roleUpdateSuccess: string = '';
  roleUpdateError: string = '';
  searchQuery: string = '';

  currentPage = 0;
  pageSize = 10;

  showDeleteConfirm = false;
  deletionCountdown: string = '';
  private countdownInterval: any;

  showCancelConfirm = false;

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  get totalPages(): number {
    return Math.ceil(this.lendings.length / this.pageSize);
  }

  get visibleLendings(): Lending[] {
    const start = this.currentPage * this.pageSize;
    return this.lendings.slice(start, start + this.pageSize);
  }

  ngOnInit(): void {
    this.authService.fetchCurrentUser().subscribe({
      next: () => this.cdr.detectChanges(),
    });

    this.route.queryParams.subscribe((params) => {
      const email = params['email'];
      if (email) {
        this.isViewingOther = true;
        this.loadProfileByEmail(email);
      } else {
        this.isViewingOther = false;
        this.loadOwnProfile();
      }
    });
  }

  get canEditRole(): boolean {
    if (!this.isViewingOther || !this.profile) return false;
    const currentUser = this.authService.currentUserValue;
    if (currentUser?.email === this.profile.email) return false;
    if (!this.authService.atLeastHasRole(Role.BIBLIOTHEEKBEHEERDER)) return false;
    if (this.profile.role === 'ADMIN') return false;
    if (this.profile.role === 'LEERLING') return false;
    return true;
  }

  get availableRoles(): string[] {
    return ['LEERKRACHT', 'BIBLIOTHEEKBEHEERDER'];
  }

  updateRole(): void {
    if (!this.profile || !this.selectedRole) return;
    this.roleUpdateSuccess = '';
    this.roleUpdateError = '';

    this.userService.updateUserRole(this.profile.email, this.selectedRole).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.selectedRole = updated.role;
        this.roleUpdateSuccess = 'Rol succesvol bijgewerkt';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.roleUpdateError = err.error ?? 'Kon rol niet bijwerken';
        this.cdr.detectChanges();
      },
    });
  }

  loadOwnProfile(): void {
    this.userService.getUser().subscribe({
      next: (data) => {
        this.profile = data;
        if (this.profile.deletionRequestedAt) this.startCountdown();
        this.loading = false;
        this.loadLendings(data.email);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.error = 'Kon profiel niet laden';
        this.loading = false;
      },
    });
  }

  loadProfileByEmail(email: string): void {
    this.userService.getProfileByEmail(email).subscribe({
      next: (data) => {
        this.profile = data;
        if (this.profile.deletionRequestedAt) this.startCountdown();
        this.selectedRole = data.role;
        this.loading = false;
        this.loadLendings(data.email);
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 403) {
          this.router.navigate(['/forbidden']);
        } else {
          console.error('Failed to load profile', err);
          this.error = 'Kon profiel niet laden';
          this.loading = false;
        }
      },
    });
  }

  loadLendings(email: string): void {
    this.lendingService.getLendingsByUser(email).subscribe({
      next: (data) => {
        const all = data ?? [];
        // Active = not returned and no dateReturned
        this.activeLendings = all.filter((l) => !l.returned && !l.dateReturned);
        // Returned only for display in the history card
        this.lendings = all.filter((l) => l.returned || !!l.dateReturned);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to load lendings', err),
    });
  }

  get filteredLendings(): Lending[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.visibleLendings;
    return this.visibleLendings.filter(
      (l) =>
        l.bookTitle?.toLowerCase().includes(q) ||
        l.bookAuthor?.toLowerCase().includes(q) ||
        l.borrowerName?.toLowerCase().includes(q),
    );
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

  get canSeeRole(): boolean {
    const role = (this.authService.currentUserValue as any)?.role?.toUpperCase();
    return role === 'ADMIN' || role === 'BIBLIOTHEEKBEHEERDER' || role === 'BEHEERDER';
  }

  get classNames(): string {
    return this.profile?.classes.map((c) => c.className).join(', ') ?? '';
  }

  get hasPendingDeletion(): boolean {
    return !!this.profile?.deletionRequestedAt;
  }

  get isDeletionStudent(): boolean {
    return this.profile?.role === 'LEERLING' && !this.isViewingOther;
  }

  get hasActiveLendings(): boolean {
    return this.activeLendings.length > 0;
  }

  openDeleteConfirm(): void {
    if (this.hasActiveLendings) return;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm = false;
  }

  confirmDeletion(): void {
    this.showDeleteConfirm = false;
    this.userService.requestDeletion().subscribe({
      next: (updated) => {
        this.profile = updated;
        this.startCountdown();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to request deletion', err),
    });
  }

  startCountdown(): void {
    this.stopCountdown();
    this.updateCountdown();
    this.countdownInterval = setInterval(() => {
      this.updateCountdown();
      this.cdr.detectChanges();
    }, 1000);
  }

  stopCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
    this.deletionCountdown = '';
  }

  updateCountdown(): void {
    if (!this.profile?.deletionRequestedAt) return;

    const rawDate = this.profile.deletionRequestedAt;
    const utcString = rawDate.endsWith('Z') ? rawDate : rawDate + 'Z'; //Z: tells JavaScript that the timestamp is in UTC
    const requestedAt = new Date(utcString);
    const deleteAt = new Date(requestedAt.getTime() + 24 * 60 * 60 * 1000);
    const diff = deleteAt.getTime() - Date.now();
    if (diff <= 0) {
      this.deletionCountdown = 'Wordt verwijderd...';
      this.stopCountdown();
      return;
    }
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    this.deletionCountdown = `${h}u ${m}m ${s}s`;
  }

  openCancelConfirm(): void {
    this.showCancelConfirm = true;
  }

  closeCancelConfirm(): void {
    this.showCancelConfirm = false;
  }

  confirmCancelDeletion(): void {
    this.showCancelConfirm = false;
    this.userService.cancelDeletion().subscribe({
      next: (updated) => {
        this.profile = updated;
        this.stopCountdown();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Failed to cancel deletion', err),
    });
  }

  ngOnDestroy(): void {
    this.stopCountdown();
  }
}
