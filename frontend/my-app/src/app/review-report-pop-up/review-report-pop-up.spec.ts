import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewReportPopUp } from './review-report-pop-up';

describe('ReviewReportPopUp', () => {
  let component: ReviewReportPopUp;
  let fixture: ComponentFixture<ReviewReportPopUp>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewReportPopUp]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewReportPopUp);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
