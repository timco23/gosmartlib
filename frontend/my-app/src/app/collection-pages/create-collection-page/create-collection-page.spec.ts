import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCollectionPage } from './create-collection-page';

describe('CreateCollectionPage', () => {
  let component: CreateCollectionPage;
  let fixture: ComponentFixture<CreateCollectionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCollectionPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateCollectionPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
