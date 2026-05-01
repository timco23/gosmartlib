import { Component, EventEmitter, inject, Input, input, OnInit, Output, output, SimpleChanges } from '@angular/core';
import { Readinglevel } from '../../../../types/types';
import { ReadingLevelService } from '../../../services/reading-level.service';

@Component({
  selector: 'app-reading-level',
  imports: [],
  templateUrl: './reading-level.component.html',
  styleUrl: './reading-level.component.css'
})
export class ReadingLevelSelectionMenuComponent {
  private readingLevelService = inject(ReadingLevelService);

  readingLevels: Readinglevel[] = [];
  selected: Readinglevel | null = null;
  @Input() initialReadingLevel: Readinglevel | null = null;
  @Output() selectedReadingLevel = new EventEmitter<Readinglevel | null>();

  ngOnInit(): void {
    this.readingLevelService.getAll().subscribe((data) => {
      this.readingLevels = data;
      if (this.initialReadingLevel) {
        this.selected = this.initialReadingLevel;
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialReadingLevel']) {
      this.selected = this.initialReadingLevel;
    }
  }

  isSelected(level: Readinglevel): boolean {
    return this.selected?.name === level.name;
  }

  toggleLevel(level: Readinglevel): void {
    if (this.isSelected(level)) {
      this.selected = null;
    } else {
      this.selected = level;
    }
    this.selectedReadingLevel.emit(this.selected);
  }
}