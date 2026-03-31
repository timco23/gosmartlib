import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LendingManagementComponent } from './lending-management.component';

describe('LendingManagementComponent', () => {
  let component: LendingManagementComponent;
  let fixture: ComponentFixture<LendingManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LendingManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LendingManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
