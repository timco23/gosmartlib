import { Component, computed, inject, signal } from '@angular/core';
import { Tag } from '../../types/types';
import { TagService } from '../tag.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tag-management',
  imports: [FormsModule],
  templateUrl: './tag-management.html',
  styleUrl: './tag-management.css',
})
export class TagManagement {
  private tagService = inject(TagService);

  tags = signal<Tag[]>([]);
  newTagName = signal('');
  editingId = signal<number | null>(null);
  editName = signal('');
  errorMessage = signal('');

  filterInput = signal('');
  filteredTags = computed(() => {
    const filter = this.filterInput().toLowerCase();
    return this.tags()
      .filter((tag) => tag.name.toLowerCase().includes(filter))
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  ngOnInit() {
    this.loadTags();
  }

  loadTags() {
    this.tagService.getTags().subscribe((tags) => this.tags.set(tags));
  }

  addTag() {
    const name = this.newTagName().trim();
    if (!name) return;

    this.tagService.addTag(name).subscribe({
      next: (tag) => {
        this.tags.update((tags) => [...tags, tag]);
        this.newTagName.set('');
        this.errorMessage.set('');
      },
      error: (err) => {
        if (err.status === 409) {
          this.errorMessage.set('Een tag met deze naam bestaat al.');
        }
      },
    });
  }

  startEdit(tag: Tag) {
    this.editingId.set(tag.id);
    this.editName.set(tag.name);
  }

  saveEdit(id: number) {
    this.tagService.updateTag(id, this.editName()).subscribe({
      next: (tag) => {
        this.loadTags();
        this.editingId.set(null);
      },
      error: (err) => {
        if (err.status === 409) {
          this.errorMessage.set('Een tag met deze naam bestaat al');
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
