import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoSchoolPage } from './no-school-page';

describe('NoSchoolPage', () => {
  let component: NoSchoolPage;
  let fixture: ComponentFixture<NoSchoolPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoSchoolPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NoSchoolPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
