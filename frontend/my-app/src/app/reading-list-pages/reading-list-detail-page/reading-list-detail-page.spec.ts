import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingListDetailPage } from './reading-list-detail-page';

describe('ReadingListDetailPage', () => {
  let component: ReadingListDetailPage;
  let fixture: ComponentFixture<ReadingListDetailPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadingListDetailPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReadingListDetailPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
