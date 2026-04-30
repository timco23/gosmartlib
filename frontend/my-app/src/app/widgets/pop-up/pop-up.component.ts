import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output, Renderer2, signal } from '@angular/core';
import { ConfirmDialogAction } from '../../../types/types';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pop-up',
  imports: [CommonModule],
  templateUrl: './pop-up.component.html',
  styleUrl: './pop-up.component.css'
})
export class PopUpComponent implements OnDestroy, OnInit {
  @Input() title?: string;
  @Input() message: string = '';
  @Input() actions: ConfirmDialogAction[] = [
    { label: 'Annuleren', type: 'cancel', value: 'cancel' },
    { label: 'Bevestigen', type: 'primary', value: 'confirm' }
  ];
  renderer = inject(Renderer2)

  @Output() actionClicked = new EventEmitter<string>();

  //Locks scrolling for body of the entire page
  ngOnInit(): void {
    this.renderer.addClass(document.body, 'no-scroll');
  }

  ngOnDestroy() {
    this.renderer.removeClass(document.body, 'no-scroll');
  }


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