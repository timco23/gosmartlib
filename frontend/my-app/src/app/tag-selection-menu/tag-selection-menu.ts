import { Component, inject, input, output, SimpleChanges } from '@angular/core';
import { Tag } from '../../types/types';
import { TagService } from '../services/tag.service';
import { FormsModule } from '@angular/forms';

/**
 * Displays all available tags and lets the user toggle which tags
 * are assigned to a specific book
 */
@Component({
  selector: 'app-tag-selection-menu',
  imports: [FormsModule],
  templateUrl: './tag-selection-menu.html',
  styleUrl: './tag-selection-menu.css',
})
export class TagSelectionMenu {
  private tagService = inject(TagService);

  bookId = input<number | null>(null);
  initialTags = input<string[]>([]);
  tagsChanged = output<string[]>();

  tags: Tag[] = [];
  filterInput: string = '';
  selectedTagNames: string[] = [];

  get filteredTags(): Tag[] {
    return this.tags
      .filter((tag) => tag.name.toLowerCase().includes(this.filterInput.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedTagCount(): number {
    return this.selectedTagNames.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialTags'] && this.tags.length > 0) {
      this.selectedTagNames = [...this.initialTags()];
    }
  }

  ngOnInit(): void {
    this.tagService.getTags().subscribe((tags) => {
      this.tags = tags;
      this.selectedTagNames = [...this.initialTags()];
    });
  }

  isTagSelected(tagName: string): boolean {
    return this.selectedTagNames.includes(tagName);
  }

  toggleTag(tagName: string): void {
    if (this.selectedTagNames.includes(tagName)) {
      this.selectedTagNames = this.selectedTagNames.filter((n) => n !== tagName);
    } else {
      this.selectedTagNames = [...this.selectedTagNames, tagName];
    }
    this.tagsChanged.emit(this.selectedTagNames);
  }
}
