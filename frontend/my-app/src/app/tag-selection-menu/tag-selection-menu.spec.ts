import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagSelectionMenu } from './tag-selection-menu';

describe('TagSelectionMenu', () => {
  let component: TagSelectionMenu;
  let fixture: ComponentFixture<TagSelectionMenu>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagSelectionMenu]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TagSelectionMenu);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
