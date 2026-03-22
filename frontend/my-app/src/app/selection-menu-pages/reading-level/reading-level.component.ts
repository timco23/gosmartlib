import { Component, inject, OnInit, output } from '@angular/core';
import { ReadingLevelService } from '../../services/reading-level.service';
import { Readinglevel } from '../../../types/types';

@Component({
  selector: 'app-reading-level',
  imports: [],
  templateUrl: './reading-level.component.html',
  styleUrl: './reading-level.component.css'
})
export class ReadingLevelSelectionMenuComponent {
  private readingLevelService = inject(ReadingLevelService);

  selectedReadingLevel = output<Readinglevel | null>();

  readingLevels: Readinglevel[] = [];
  selected: Readinglevel | null = null;

  ngOnInit(): void {
    this.readingLevelService.getAll().subscribe((data) => {
      this.readingLevels = data;
    });
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