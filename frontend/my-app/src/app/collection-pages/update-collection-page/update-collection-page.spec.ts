import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateCollectionPage } from './update-collection-page';

describe('UpdateCollectionPage', () => {
  let component: UpdateCollectionPage;
  let fixture: ComponentFixture<UpdateCollectionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateCollectionPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateCollectionPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
