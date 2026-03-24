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
      console.log(this.filteredTags());
    });
  }

  loadGenres() {
    this.genreService.getGenres().subscribe((genres) => {
      this.genres.set(genres);
      console.log(this.filteredGenres());
    });
  }

  loadSubgenres() {
    this.subgenreService.getSubgenres().subscribe((subgenres) => {
      this.subgenres.set(subgenres);
      console.log(this.filteredSubgenres());
    });
  }

  loadDidacticCategories() {
    this.didacticService.getDidacticCategories().subscribe((didacticCategories) => {
      this.didacticCategories.set(didacticCategories);
      console.log(this.filteredDidacticCategories());
    });
  }

  addTag() {
    const name = this.newTagName().trim();
    if (!name) return;
    this.addErrorMessage.set('');

    this.tagService.addTag(name).subscribe({
      next: (tag) => {
        this.tags.update((tags) => [...tags, tag]);
        this.newTagName.set('');
        this.addErrorMessage.set('');
      },
      error: (err) => {
        if (err.status === 409) {
          this.addErrorMessage.set('Een tag met deze naam bestaat al.');
        }
      },
    });
  }

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
