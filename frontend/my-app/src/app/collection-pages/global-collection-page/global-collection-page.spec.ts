import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalCollectionPage } from './global-collection-page';

describe('GlobalCollectionPage', () => {
  let component: GlobalCollectionPage;
  let fixture: ComponentFixture<GlobalCollectionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalCollectionPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GlobalCollectionPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
