import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateBookPage } from './update-book-page';

describe('UpdateBookPage', () => {
  let component: UpdateBookPage;
  let fixture: ComponentFixture<UpdateBookPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateBookPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateBookPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
