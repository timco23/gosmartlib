import { Component, effect, inject, input, output, SimpleChanges } from '@angular/core';
import { Genre, Role, Subgenre, Tag } from '../../../types/types';
import { AuthService } from '../../services/auth.service';
import { SubgenreService } from '../../services/subgenre.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-subgenre-selection-menu',
  imports: [FormsModule],
  templateUrl: './subgenre-selection-menu.component.html',
  styleUrl: './subgenre-selection-menu.component.css'
})
export class SubgenreSelectionMenuComponent {
  private subgenreService = inject(SubgenreService);
  authService = inject(AuthService)
  role = Role;

  bookId = input<number | null>(null);
  initialSubgenres = input<Subgenre[]>([]);
  subgenresChanged = output<Subgenre[]>();

  subgenres: Subgenre[] = [];
  filterInput: string = '';
  selectedSubgenres: Subgenre[] = [];
  genreIds = input<Genre[] | null>(null);

  constructor() {
    effect(() => {
      const genres = this.genreIds();
      if (genres != null && genres.length > 0) {
        const ids = genres.map(g => g.id);
        console.log(genres)
        this.subgenreService.getSubgenresByGenres(ids).subscribe((subgenres) => {
          this.subgenres = subgenres;
        });
      } else {
        this.subgenres = [];
      }
    });
  }

  get filteredSubgenres(): Subgenre[] {
    return this.subgenres
      .filter((subgenre) => subgenre.name.toLowerCase().includes(this.filterInput.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedSubgenreCount(): number {
    return this.selectedSubgenres.length;
  }

  private loadSubgenres(): void {
    const genres = this.genreIds();
    if (genres != null && genres.length > 0) {
      const ids = genres.map(g => g.id);
      this.subgenreService.getSubgenresByGenres(ids).subscribe((subgenres) => {
        this.subgenres = subgenres;
      });
    } else {
      this.subgenres = [];
    }
  }

  isSubgenreSelected(subgenre: Subgenre): boolean {
    return this.selectedSubgenres.some((g) => g.name === subgenre.name);
  }

  toggleSubgenre(subgenre: Subgenre): void {
    if (this.isSubgenreSelected(subgenre)) {
      this.selectedSubgenres = this.selectedSubgenres.filter((g) => g.name !== subgenre.name);
    } else {
      this.selectedSubgenres.push(subgenre);
    }
    this.subgenresChanged.emit(this.selectedSubgenres);
  }
}
