import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollectionListPage } from './collection-list-page';

describe('CollectionListPage', () => {
  let component: CollectionListPage;
  let fixture: ComponentFixture<CollectionListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionListPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CollectionListPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
