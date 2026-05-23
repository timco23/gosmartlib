import { Component, effect, EventEmitter, inject, Input, model, OnChanges, OnInit, Output, Renderer2, signal, SimpleChanges } from '@angular/core';
import { UserProfile } from '../../types/types';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-user-selector-popup',
  imports: [CommonModule, FormsModule],
  templateUrl: './user-selector-popup.component.html',
  styleUrl: './user-selector-popup.component.css'
})
export class UserSelectorPopupComponent {
  renderer = inject(Renderer2);
  userService = inject(UserService)

  isOpen = model<boolean>(false);
  users = signal<UserProfile[]>([]);
  @Input() limit = 20;
  @Input() selectedId: string | number | null = null;

  @Output() userSelected = new EventEmitter<UserProfile>();
  @Output() close = new EventEmitter<void>();

  searchTerm = '';

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
    this.getUsers();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen()) {
      this.searchTerm = '';
    }
    if (changes['isOpen'] || changes['users']) {
      this.getUsers();
    }
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

  getUsers() {
    this.userService.getFilteredUsers(0, this.searchTerm).subscribe({
      next: (data) => {
        this.users.set(data.content);
      }
    })
  }
}