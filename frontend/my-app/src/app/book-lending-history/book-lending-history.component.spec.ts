import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookLendingHistoryComponent } from './book-lending-history.component';

describe('BookLendingHistoryComponent', () => {
  let component: BookLendingHistoryComponent;
  let fixture: ComponentFixture<BookLendingHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookLendingHistoryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookLendingHistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
