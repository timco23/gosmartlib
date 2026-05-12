import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, Renderer2, SimpleChanges, effect, inject, model } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-lending-selector-popup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './lending-selector-popup.component.html',
  styleUrl: './lending-selector-popup.component.css'
})
export class LendingSelectorPopupComponent implements OnInit, OnChanges {
  renderer = inject(Renderer2);

  isOpen = model<boolean>(false);
  @Input() lendings: any[] = [];
  @Input() limit = 50;
  @Input() selectedId: number | null = null;

  @Output() lendingSelected = new EventEmitter<any>();
  @Output() close = new EventEmitter<void>();

  searchTerm = '';
  filteredLendings: any[] = [];

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
    if (changes['isOpen'] || changes['lendings']) {
      this.applyFilter();
    }
  }

  applyFilter(): void {
    const q = this.normalize(this.searchTerm);
    const list = !q
      ? this.lendings
      : this.lendings.filter((l) => {
          const haystack = this.normalize(`${l.bookTitle} ${l.borrowerName} ${l.copyBarcode}`);
          return haystack.includes(q);
        });
    this.filteredLendings = list.slice(0, this.limit);
  }

  isSelected(lending: any): boolean {
    if (this.selectedId === null) return false;
    return lending.id === this.selectedId;
  }

  selectLending(lending: any): void {
    this.lendingSelected.emit(lending);
    this.closePopup();
  }

  trackByLending = (_: number, lending: any): number => lending.id;

  closePopup(): void {
    this.isOpen.set(false);
    this.close.emit();
  }

  private normalize(value: string | null | undefined): string {
    return (value ?? '').toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '').trim();
  }
}