import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Campus, CampusCopyGroup } from '../../types/types';

@Component({
  selector: 'app-campus-copy-group',
  imports: [FormsModule],
  templateUrl: './campus-copy-group.component.html',
  styleUrl: './campus-copy-group.component.css',
})
export class CampusCopyGroupComponent {
  @Input({ required: true }) group!: CampusCopyGroup;
  @Input({ required: true }) campuses: Campus[] = [];
  @Input() label: string = '';
  @Input() removable: boolean = false;

  @Output() removed = new EventEmitter<void>();
  @Output() groupChanged = new EventEmitter<void>();

  onCopyCountChange() {
    const count = Math.max(1, this.group.copyCount || 1);
    const current = this.group.rows.length;
    if (count > current) {
      for (let i = current; i < count; i++) {
        this.group.rows.push({ condition: 'NIEUW', copy_status: 'BESCHIKBAAR' });
      }
    } else {
      this.group.rows.length = count;
    }
    this.groupChanged.emit();
  }
}
