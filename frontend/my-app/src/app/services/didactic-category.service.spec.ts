import { TestBed } from '@angular/core/testing';

import { DidacticCategoryService } from '../services/didactic-category.service';

describe('DidacticCategoryService', () => {
  let service: DidacticCategoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DidacticCategoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
