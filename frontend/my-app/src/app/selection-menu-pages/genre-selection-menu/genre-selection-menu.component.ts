import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  inject,
  Input,
  input,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GenreService } from '../../services/genre.service';
import { AuthService } from '../../services/auth.service';
import { Genre, Role } from '../../../types/types';

@Component({
  selector: 'app-genre-selection-menu',
  imports: [FormsModule],
  templateUrl: './genre-selection-menu.component.html',
  styleUrl: './genre-selection-menu.component.css',
})
export class GenreSelectionMenuComponent {
  private genreService = inject(GenreService);
  private cdr = inject(ChangeDetectorRef);
  authService = inject(AuthService);
  role = Role;

  bookId = input<number | null>(null);
  @Input() initialGenres: Genre[] = [];

  @Output() genresChanged = new EventEmitter<Genre[]>();

  genres: Genre[] = [];
  filterInput: string = '';
  selectedGenres: Genre[] = [];

  get filteredGenres(): Genre[] {
    return this.genres
      .filter((genre) => genre.name.toLowerCase().includes(this.filterInput.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedGenreCount(): number {
    return this.selectedGenres.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialGenres'] && this.initialGenres) {
      this.selectedGenres = [...this.initialGenres];
    }
  }

  ngOnInit(): void {
    this.genreService.getGenres().subscribe((genres) => {
      this.genres = genres;
      this.selectedGenres = [...this.initialGenres];
      this.cdr.detectChanges();
    });
  }

  isGenreSelected(genre: Genre): boolean {
    return this.selectedGenres.some((g) => g.name === genre.name);
  }

  toggleGenre(genre: Genre): void {
    if (this.isGenreSelected(genre)) {
      this.selectedGenres = this.selectedGenres.filter((g) => g.name !== genre.name);
    } else {
      this.selectedGenres.push(genre);
    }
    this.genresChanged.emit(this.selectedGenres);
  }
}
