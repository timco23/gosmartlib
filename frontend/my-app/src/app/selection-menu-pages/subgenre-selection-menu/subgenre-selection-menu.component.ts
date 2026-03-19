import { Component, inject, input, output, SimpleChanges } from '@angular/core';
import { Role, Subgenre } from '../../../types/types';
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
  tagsChanged = output<Subgenre[]>();

  subgenres: Subgenre[] = [];
  filterInput: string = '';
  selectedSubgenres: Subgenre[] = [];

    get filteredSubgenres(): Subgenre[] {
      return this.subgenres
        .filter((subgenre) => subgenre.name.toLowerCase().includes(this.filterInput.toLowerCase()))
        .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedSubgenreCount(): number {
    return this.selectedSubgenres.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialTags']) {
      this.selectedSubgenres = [...this.initialSubgenres()];
    }
  }

  ngOnInit(): void {
    this.subgenreService.getSubgenres().subscribe((subgenre) => {
      this.subgenres = subgenre;
      this.selectedSubgenres = [...this.initialSubgenres()];
    });
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
    this.tagsChanged.emit(this.selectedSubgenres);
  }
}
