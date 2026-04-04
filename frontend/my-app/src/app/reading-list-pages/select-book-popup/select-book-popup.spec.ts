import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectBookPopup } from './select-book-popup';

describe('SelectBookPopup', () => {
  let component: SelectBookPopup;
  let fixture: ComponentFixture<SelectBookPopup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectBookPopup]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectBookPopup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
