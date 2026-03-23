import { ChangeDetectorRef, Component, EventEmitter, inject, Input, input, Output, output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DidacticCategoryService } from '../../services/didactic-category.service';
import { DidacticCategory } from '../../../types/types';

@Component({
  selector: 'app-didactic-category-selection-menu',
  imports: [FormsModule],
  templateUrl: './didactic-category-selection-menu.component.html',
  styleUrl: './didactic-category-selection-menu.component.css'
})
export class DidacticCategorySelectionMenuComponent {
  private didacticCategoryService = inject(DidacticCategoryService);
  cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  @Input() initialCategories: DidacticCategory[] = [];
  @Output() tagsChanged = new EventEmitter<DidacticCategory[]>();

  categories: DidacticCategory[] = [];
  filterInput: string = '';
  selectedCategories: DidacticCategory[] = [];

  get filteredCategories(): DidacticCategory[] {
    return this.categories
      .filter((category) => category.name.toLowerCase().includes(this.filterInput.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get selectedCategoryCount(): number {
    return this.selectedCategories.length;
  }

  ngOnChanges(changes: SimpleChanges): void {
  if (changes['initialCategories'] && this.initialCategories) {
    this.selectedCategories = [...this.initialCategories];
    this.cdr.detectChanges();
  }
}

  ngOnInit(): void {
    this.didacticCategoryService.getDidacticCategories().subscribe((categories) => {
      this.categories = categories;
      this.selectedCategories = [...this.initialCategories];
      this.cdr.detectChanges();

    });
  }

  isCategorySelected(category: DidacticCategory): boolean {
    return this.selectedCategories.some((c) => c.name === category.name);
  }

  toggleCategory(category: DidacticCategory): void {
    if (this.isCategorySelected(category)) {
      this.selectedCategories = this.selectedCategories.filter((c) => c.name !== category.name);
    } else {
      this.selectedCategories.push(category);
    }
    this.tagsChanged.emit(this.selectedCategories);
  }
}