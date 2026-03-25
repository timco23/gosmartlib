import { TestBed } from '@angular/core/testing';

import { SubgenreService } from '../subgenre.service';

describe('SubgenreService', () => {
  let service: SubgenreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SubgenreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
