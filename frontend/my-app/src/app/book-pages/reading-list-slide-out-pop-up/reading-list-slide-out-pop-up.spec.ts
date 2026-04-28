import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingListSlideOutPopUp } from './reading-list-slide-out-pop-up';

describe('ReadingListSlideOutPopUp', () => {
  let component: ReadingListSlideOutPopUp;
  let fixture: ComponentFixture<ReadingListSlideOutPopUp>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadingListSlideOutPopUp]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReadingListSlideOutPopUp);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
