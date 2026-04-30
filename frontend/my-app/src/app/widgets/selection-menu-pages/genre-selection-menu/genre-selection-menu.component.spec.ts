import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenreSelectionMenuComponent } from './genre-selection-menu.component';

describe('GenreSelectionMenuComponent', () => {
  let component: GenreSelectionMenuComponent;
  let fixture: ComponentFixture<GenreSelectionMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenreSelectionMenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenreSelectionMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
