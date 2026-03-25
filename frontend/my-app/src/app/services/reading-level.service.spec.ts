import { TestBed } from '@angular/core/testing';

import { ReadingLevelService } from '../reading-level.service';

describe('ReadingLevelService', () => {
  let service: ReadingLevelService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReadingLevelService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
