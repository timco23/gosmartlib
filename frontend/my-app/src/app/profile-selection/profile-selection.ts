import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserProfile, Role } from '../../types/types';
import { UserService } from '../services/user.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-profile-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-selection.html',
  styleUrl: './profile-selection.css',
})
export class ProfileSelectionComponent implements OnInit {
  users: UserProfile[] = [];
  filteredUsers: UserProfile[] = [];
  nameFilter: string = '';
  loading: boolean = true;
  error: string = '';
  isAdmin: boolean = false;
  sortColumn: string = 'email';
  sortDirection: 'asc' | 'desc' = 'asc';

  currentPage = 0;
  readonly pageSize = 10;

  get totalPages(): number {
    return Math.ceil(this.filteredUsers.length / this.pageSize);
  }

  get pagedUsers(): UserProfile[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }
  
  constructor(
    private userService: UserService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.atLeastHasRole(Role.ADMIN);
    this.userService.getSchoolUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.applyFilterAndSort();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load school users', err);
        this.error = 'Kon gebruikers niet laden';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }
  
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) this.currentPage++;
  }

  previousPage(): void {
    if (this.currentPage > 0) this.currentPage--;
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.applyFilterAndSort();
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.applyFilterAndSort();
  }
  
  private applyFilterAndSort(): void {
    const filter = this.nameFilter.toLowerCase().trim();
    let result = filter
      ? this.users.filter((u) => {
          const fullName = `${u.firstName ?? ''} ${u.name ?? ''}`.trim().toLowerCase();
          return fullName.includes(filter);
        })
      : [...this.users];

    result.sort((a, b) => {
      let aVal = '';
      let bVal = '';
      switch (this.sortColumn) {
        case 'naam':
          aVal = `${a.firstName ?? ''} ${a.name ?? ''}`.toLowerCase();
          bVal = `${b.firstName ?? ''} ${b.name ?? ''}`.toLowerCase();
          break;
        case 'email':
          aVal = (a.email ?? '').toLowerCase();
          bVal = (b.email ?? '').toLowerCase();
          break;
        case 'klas':
          aVal = (a.classes[0].className ?? '').toLowerCase();
          bVal = (b.classes[0].className ?? '').toLowerCase();
          break;
        case 'rol':
          aVal = (a.role ?? '').toLowerCase();
          bVal = (b.role ?? '').toLowerCase();
          break;
      }
      const cmp = aVal.localeCompare(bVal);
      return this.sortDirection === 'asc' ? cmp : -cmp;
    });

    this.filteredUsers = result;
  }

  openProfile(user: UserProfile): void {
    this.router.navigate(['/profile'], { queryParams: { email: user.email } });
  }

  getClasses(user: UserProfile): string {
    if (user.classes.length > 0) {
      return user.classes.map((clazz) => clazz.className).join(' ');
    }
    return '-';
  }
}
