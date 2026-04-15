import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookSelectorPopupComponent } from './book-selector-popup-component';

describe('BookSelectorPopupComponent', () => {
  let component: BookSelectorPopupComponent;
  let fixture: ComponentFixture<BookSelectorPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookSelectorPopupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookSelectorPopupComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
