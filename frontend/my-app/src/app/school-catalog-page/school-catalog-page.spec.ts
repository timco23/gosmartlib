import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchoolCatalogPage } from './school-catalog-page';

describe('SchoolCatalogPage', () => {
  let component: SchoolCatalogPage;
  let fixture: ComponentFixture<SchoolCatalogPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchoolCatalogPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SchoolCatalogPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
