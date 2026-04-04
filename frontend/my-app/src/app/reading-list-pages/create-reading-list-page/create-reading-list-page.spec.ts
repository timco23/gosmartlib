import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateReadingListPage } from './create-reading-list-page';

describe('CreateReadingListPage', () => {
  let component: CreateReadingListPage;
  let fixture: ComponentFixture<CreateReadingListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateReadingListPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateReadingListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
