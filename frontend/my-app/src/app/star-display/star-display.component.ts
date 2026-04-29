import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-star-display',
  imports: [],
  templateUrl: './star-display.component.html',
  styleUrl: './star-display.component.css',
})
export class StarDisplayComponent {
  @Input() score: number = 0;
  @Input() interactive: boolean = false;
  @Input() size: number = 20;
  @Output() scoreChange = new EventEmitter<number>();

  private static idCounter = 0;
  readonly instanceId = ++StarDisplayComponent.idCounter;

  hoverScore = 0;

  private get displayScore(): number {
    return this.interactive ? (this.hoverScore || this.score) : this.score;
  }

  get stars(): { fill: number }[] {
    return Array.from({ length: 5 }, (_, i) => ({
      fill: Math.min(1, Math.max(0, this.displayScore - i)),
    }));
  }

  onMouseEnter(index: number): void {
    if (this.interactive) this.hoverScore = index + 1;
  }

  onMouseLeave(): void {
    if (this.interactive) this.hoverScore = 0;
  }

  onClick(index: number): void {
    if (this.interactive) this.scoreChange.emit(index + 1);
  }
}
