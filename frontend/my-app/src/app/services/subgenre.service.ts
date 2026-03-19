import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SubgenreService {
    private apiUrl = `${environment.apiUrl}/subgenres`;
  

  constructor() { }
}
