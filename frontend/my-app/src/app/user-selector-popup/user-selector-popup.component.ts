import { Component, effect, EventEmitter, inject, Input, model, OnChanges, OnInit, Output, Renderer2, SimpleChanges } from '@angular/core';
import { UserProfile } from '../../types/types';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-selector-popup',
  imports: [CommonModule, FormsModule],
  templateUrl: './user-selector-popup.component.html',
  styleUrl: './user-selector-popup.component.css'
})
export class UserSelectorPopupComponent {
  renderer = inject(Renderer2);

  isOpen = model<boolean>(false);
  @Input() users: UserProfile[] = [];
  @Input() limit = 20;
  @Input() selectedId: string | number | null = null;

  @Output() userSelected = new EventEmitter<UserProfile>();
  @Output() close = new EventEmitter<void>();

  searchTerm = '';
  filteredUsers: UserProfile[] = [];

  constructor() {
    effect(() => {
      if (this.isOpen()) {
        this.renderer.addClass(document.body, 'no-scroll');
      } else {
        this.renderer.removeClass(document.body, 'no-scroll');
      }
    });
  }

  ngOnInit(): void {
    this.applyFilter();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen()) {
      this.searchTerm = '';
    }
    if (changes['isOpen'] || changes['users']) {
      this.applyFilter();
    }
  }

  applyFilter(): void {
    const q = this.normalize(this.searchTerm);
    const list = !q
      ? this.users
      : this.users.filter((u) => {
          const haystack = this.normalize(`${u.name} ${u.email} ${this.getClasses(u)}`);
          return haystack.includes(q);
        });
    this.filteredUsers = list.slice(0, this.limit);
  }

  isSelected(user: UserProfile): boolean {
    if (this.selectedId === null) return false;
    return String(user.id) === String(this.selectedId);
  }

  selectUser(user: UserProfile): void {
    this.userSelected.emit(user);
    this.closePopup();
  }

  getClasses(user: UserProfile): string {
    return user.classes?.map(c => c.className).join(' ') ?? '';
  }

  trackByUser = (_: number, user: UserProfile): string | number => user.id ?? '';

  closePopup(): void {
    this.isOpen.set(false);
    this.close.emit();
  }

  private normalize(value: string | null | undefined): string {
    return (value ?? '').toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '').trim();
  }
}