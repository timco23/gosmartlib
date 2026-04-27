import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden-component',
  imports: [CommonModule, RouterLink],
  templateUrl: './forbidden-component.html',
  styleUrl: './forbidden-component.css',
})
export class ForbiddenComponent {
  constructor(private route: ActivatedRoute) {}

  get from(): string {
    return this.route.snapshot.queryParamMap.get('from') ?? '';
  }
}
