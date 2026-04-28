import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-star-display',
  imports: [],
  templateUrl: './star-display.component.html',
  styleUrl: './star-display.component.css',
})
export class StarDisplayComponent {
  @Input() score: number = 0;

  private static idCounter = 0;
  readonly instanceId = ++StarDisplayComponent.idCounter;

  get stars(): { fill: number }[] {
    return Array.from({ length: 5 }, (_, i) => ({
      fill: Math.min(1, Math.max(0, this.score - i)),
    }));
  }
}
