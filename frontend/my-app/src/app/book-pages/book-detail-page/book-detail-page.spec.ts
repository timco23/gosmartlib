import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookDetailPage } from './book-detail-page';
import { PopUpComponent } from '../../widgets/pop-up/pop-up.component';

describe('BookDetailPage', () => {
  let component: BookDetailPage;
  let fixture: ComponentFixture<BookDetailPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookDetailPage, PopUpComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookDetailPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
