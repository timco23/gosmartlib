import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DidacticCategorySelectionMenuComponent } from './didactic-category-selection-menu.component';

describe('DidacticCategorySelectionMenuComponent', () => {
  let component: DidacticCategorySelectionMenuComponent;
  let fixture: ComponentFixture<DidacticCategorySelectionMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DidacticCategorySelectionMenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DidacticCategorySelectionMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
