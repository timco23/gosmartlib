import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateReadingListPage } from './update-reading-list-page';

describe('UpdateReadingListPage', () => {
  let component: UpdateReadingListPage;
  let fixture: ComponentFixture<UpdateReadingListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateReadingListPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateReadingListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
