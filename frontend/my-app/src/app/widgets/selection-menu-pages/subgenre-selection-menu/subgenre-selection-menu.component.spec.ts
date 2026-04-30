import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubgenreSelectionMenuComponent } from './subgenre-selection-menu.component';

describe('SubgenreSelectionMenuComponent', () => {
  let component: SubgenreSelectionMenuComponent;
  let fixture: ComponentFixture<SubgenreSelectionMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubgenreSelectionMenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubgenreSelectionMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
