import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingLevelComponent } from './reading-level.component';

describe('ReadingLevelComponent', () => {
  let component: ReadingLevelComponent;
  let fixture: ComponentFixture<ReadingLevelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReadingLevelComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReadingLevelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
