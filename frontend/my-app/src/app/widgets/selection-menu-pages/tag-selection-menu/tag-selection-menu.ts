import { Component, EventEmitter, inject, Input, input, Output, output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Role, Tag } from '../../../../types/types';
import { AuthService } from '../../../services/auth.service';
import { TagService } from '../../../services/tag.service';

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
  authService = inject(AuthService)
  role = Role;

  bookId = input<number | null>(null);
  @Input() initialTags: Tag[] = [];
  @Output() tagsChanged = new EventEmitter<Tag[]>();

  tags: Tag[] = [];
  filterInput: string = '';
  selectedTags: Tag[] = [];

  get filteredTags(): Tag[] {
    return this.tags
      .filter((tag) => tag.name.toLowerCase().includes(this.filterInput.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedTagCount(): number {
    return this.selectedTags.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
  if (changes['initialTags'] && this.initialTags) {
    this.selectedTags = [...this.initialTags];
  }
}

  ngOnInit(): void {
    this.tagService.getTags().subscribe((tags) => {
      this.tags = tags;
      this.selectedTags = [...this.initialTags];
    });
  }

  isTagSelected(tag: Tag): boolean {
    return this.selectedTags.some((t) => t.name === tag.name);
  }

  toggleTag(tag: Tag): void {
    if (this.isTagSelected(tag)) {
      this.selectedTags = this.selectedTags.filter((t) => t.name !== tag.name);
    } else {
      this.selectedTags = [...this.selectedTags, tag];
    }
    this.tagsChanged.emit(this.selectedTags);
  }
}
