import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingListPage } from './reading-list-page';

describe('ReadingListPage', () => {
  let component: ReadingListPage;
  let fixture: ComponentFixture<ReadingListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadingListPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReadingListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
