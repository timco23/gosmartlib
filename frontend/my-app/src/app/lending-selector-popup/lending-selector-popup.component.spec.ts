import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LendingSelectorPopupComponent } from './lending-selector-popup.component';

describe('LendingSelectorPopupComponent', () => {
  let component: LendingSelectorPopupComponent;
  let fixture: ComponentFixture<LendingSelectorPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LendingSelectorPopupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LendingSelectorPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
