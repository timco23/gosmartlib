import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShareListPopup } from './share-list-popup';

describe('ShareListPopup', () => {
  let component: ShareListPopup;
  let fixture: ComponentFixture<ShareListPopup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShareListPopup]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShareListPopup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
