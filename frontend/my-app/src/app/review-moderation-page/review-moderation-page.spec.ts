import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewModerationPage } from './review-moderation-page';

describe('ReviewModerationPage', () => {
  let component: ReviewModerationPage;
  let fixture: ComponentFixture<ReviewModerationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewModerationPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewModerationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
