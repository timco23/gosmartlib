import { Component, computed, inject, signal } from '@angular/core';
import { DidacticCategory, Genre, Subgenre, Tag } from '../../types/types';
import { TagService } from '../services/tag.service';
import { FormsModule } from '@angular/forms';
import { GenreService } from '../services/genre.service';
import { SubgenreService } from '../services/subgenre.service';
import { DidacticCategoryService } from '../services/didactic-category.service';

@Component({
  selector: 'app-tag-management',
  imports: [FormsModule],
  templateUrl: './tag-management.component.html',
  styleUrl: './tag-management.component.css',
})
export class TagManagement {
  private tagService = inject(TagService);
  private genreService = inject(GenreService);
  private subgenreService = inject(SubgenreService);
  private didacticService = inject(DidacticCategoryService);

  tags = signal<Tag[]>([]);
  genres = signal<Genre[]>([]);
  subgenres = signal<Subgenre[]>([]);
  didacticCategories = signal<DidacticCategory[]>([]);

  newTagName = signal('');
  editingId = signal<number | null>(null); //id of the tag currently being edited
  editName = signal('');
  addErrorMessage = signal('');
  editErrorMessage = signal('');
  filterInput = signal('');

  selectedType = signal<'tags' | 'genres' | 'didacticCategories'>('tags');

  selectedParentGenreId = signal<string | null>(null);
  newSubgenreName = signal<string>('');

  //case-insensitive keyword filtering
  filteredTags = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.tags()
      .filter((tag) => tag.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  filteredGenres = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.genres()
      .filter((genre) => genre.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  filteredSubgenres = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.subgenres()
      .filter((subgenre) => subgenre.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  filteredDidacticCategories = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.didacticCategories()
      .filter((didacticCategory) => didacticCategory.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  ngOnInit() {
    this.loadTags();
    this.loadGenres();
    this.loadSubgenres();
    this.loadDidacticCategories();
  }

  loadTags() {
    this.tagService.getTags().subscribe((tags) => {
      this.tags.set(tags);
    });
  }

  loadGenres() {
    this.genreService.getGenres().subscribe((genres) => {
      this.genres.set(genres);
    });
  }

  asGenre(item: any): Genre {
    return item as Genre;
  }

  loadSubgenres() {
    this.subgenreService.getSubgenres().subscribe((subgenres) => {
      this.subgenres.set(subgenres);
    });
  }

  loadDidacticCategories() {
    this.didacticService.getDidacticCategories().subscribe((didacticCategories) => {
      this.didacticCategories.set(didacticCategories);
    });
  }

  addItem() {
    const name = this.newTagName().trim();
    if (!name) return;
    this.addErrorMessage.set('');

    const type = this.selectedType();

    if (type === 'tags') {
      this.tagService.addTag(name).subscribe({
        next: (tag) => { this.tags.update(t => [...t, tag]); this.newTagName.set(''); },
        error: (err) => { if (err.status === 409) this.addErrorMessage.set('Bestaat al.'); }
      });
    } else if (type === 'genres') {
      this.genreService.addGenre(name).subscribe({
        next: (genre) => { this.genres.update(g => [...g, genre]); this.newTagName.set(''); },
        error: (err) => { if (err.status === 409) this.addErrorMessage.set('Bestaat al.'); }
      });
    } else if (type === 'didacticCategories') {
      this.didacticService.addDidacticCategory(name).subscribe({
        next: (cat) => { this.didacticCategories.update(c => [...c, cat]); this.newTagName.set(''); },
        error: (err) => { if (err.status === 409) this.addErrorMessage.set('Bestaat al.'); }
      });
    }
  }

  addSubgenre() {
    const parentId = this.selectedParentGenreId();
    const name = this.newSubgenreName().trim();

    if (!parentId || !name) return;
      this.subgenreService.addSubgenre(name, +parentId).subscribe({
        next: (subgenre) => {
          this.genres.update(genres =>
            genres.map(g =>
              g.id === +parentId
                ? { ...g, subgenres: [...(g.subgenres ?? []), subgenre] }
                : g
            )
          );
          this.newSubgenreName.set('');
        },
        error: (err) => {
          if (err.status === 409) this.addErrorMessage.set('Bestaat al.');
        }
      });
      this.newSubgenreName.set('');
  }

  deleteItem(id: number) {
    const type = this.selectedType();
    if (type === 'tags') {
      this.tagService.deleteTag(id).subscribe(() => this.tags.update(t => t.filter(x => x.id !== id)));
    } else if (type === 'genres') {
      this.genreService.deleteGenre(id).subscribe(() => this.genres.update(g => g.filter(x => x.id !== id)));
    } else if (type === 'didacticCategories') {
      this.didacticService.deleteDidacticCategory(id).subscribe(() => this.didacticCategories.update(c => c.filter(x => x.id !== id)));
    }
  }

  saveEditItem(id: number) {
    this.editErrorMessage.set('');
    const type = this.selectedType();
    const reload = () => { this.editingId.set(null); };

    if (type === 'tags') {
      this.tagService.updateTag(id, this.editName()).subscribe({ next: () => { this.loadTags(); reload(); }, error: (err) => { if (err.status === 409) this.editErrorMessage.set('Bestaat al.'); } });
    } else if (type === 'genres') {
      this.genreService.updateGenre(id, this.editName()).subscribe({ next: () => { this.loadGenres(); reload(); }, error: (err) => { if (err.status === 409) this.editErrorMessage.set('Bestaat al.'); } });
    } else if (type === 'didacticCategories') {
      this.didacticService.updateDidacticCategory(id, this.editName()).subscribe({ next: () => { this.loadDidacticCategories(); reload(); }, error: (err) => { if (err.status === 409) this.editErrorMessage.set('Bestaat al.'); } });
    }
  }

  saveSubgenreEdit(id: number) {
    this.subgenreService.updateSubgenre(id, this.editName()).subscribe({
      next: () => { this.loadGenres(); this.editingId.set(null); },
      error: (err) => { if (err.status === 409) this.editErrorMessage.set('Bestaat al.'); }
    });
  }

  deleteSubgenre(subId: number, genreId: number) {
    this.subgenreService.deleteSubgenre(subId).subscribe(() => {
      this.genres.update(genres =>
        genres.map(g =>
          g.id === genreId
            ? { ...g, subgenres: (g.subgenres ?? []).filter(s => s.id !== subId) }
            : g
        )
      );
    });
  }

  currentList = computed(() => {
    const type = this.selectedType();
    if (type === 'tags') return this.filteredTags();
    if (type === 'genres') return this.filteredGenres();
    return this.filteredDidacticCategories();
  });

  startEdit(tag: Tag) {
    this.editingId.set(tag.id);
    this.editName.set(tag.name);
  }

  cancelEdit() {
    this.editingId.set(null);
    this.editErrorMessage.set('');
  }

  saveEdit(id: number) {
    this.editErrorMessage.set('');
    this.tagService.updateTag(id, this.editName()).subscribe({
      next: () => {
        //reload
        this.loadTags();
        this.editingId.set(null);
      },
      error: (err) => {
        if (err.status === 409) {
          this.editErrorMessage.set('Een tag met deze naam bestaat al');
        }
      },
    });
  }

  deleteTag(id: number) {
    this.tagService
      .deleteTag(id)
      .subscribe(() => this.tags.update((tags) => tags.filter((t) => t.id !== id)));
  }
}
