import { Component } from '@angular/core';
import { Lending } from '../../types/types';
import { LendingService } from '../services/lending.service';
import { DatePipe, JsonPipe } from '@angular/common';

@Component({
  selector: 'app-lending-management',
  imports: [DatePipe, JsonPipe],
  templateUrl: './lending-management.component.html',
  styleUrl: './lending-management.component.css'
})
export class LendingManagementComponent {
  lendingService: LendingService;
  public lendings: Lending[] = [];

  constructor(lendingservice: LendingService) {
    this.lendingService = lendingservice;
  }

  public ngOnInit(): void {
    this.lendingService.getLendings().subscribe((data: Lending[]) => {
    this.lendings = data;
    console.log('Lendings:', this.lendings);
    });
  }
}
