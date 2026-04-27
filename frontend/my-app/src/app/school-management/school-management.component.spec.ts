import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchoolManagementComponent } from './school-management.component';
import { PopUpComponent } from '../pop-up/pop-up.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('SchoolManagementComponent', () => {
  let component: SchoolManagementComponent;
  let fixture: ComponentFixture<SchoolManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        SchoolManagementComponent,
        ReactiveFormsModule,
        PopUpComponent
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SchoolManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
