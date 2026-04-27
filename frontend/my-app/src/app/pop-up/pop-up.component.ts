import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ConfirmDialogAction } from '../../types/types';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pop-up',
  imports: [CommonModule],
  templateUrl: './pop-up.component.html',
  styleUrl: './pop-up.component.css'
})
export class PopUpComponent {
  @Input() title?: string;
  @Input() message: string = '';
  @Input() actions: ConfirmDialogAction[] = [
    { label: 'Annuleren', type: 'cancel', value: 'cancel' },
    { label: 'Bevestigen', type: 'primary', value: 'confirm' }
  ];

  @Output() actionClicked = new EventEmitter<string>();

  onAction(value: string) {
    this.actionClicked.emit(value);
  }

  getButtonClass(action: ConfirmDialogAction): string {
    const map: Record<string, string> = {
      danger: 'confirm-delete-btn',
      cancel: 'cancel-button',
      primary: 'confirm-btn'
    };
    return map[action.type ?? 'primary'];
  }
}