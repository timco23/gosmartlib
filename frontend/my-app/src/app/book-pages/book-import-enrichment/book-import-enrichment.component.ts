import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { BookImportRow, EnrichedRowState, School } from '../../../types/types';

@Component({
  selector: 'app-book-import-enrichment',
  imports: [FormsModule, NgClass],
  templateUrl: './book-import-enrichment.component.html',
  styleUrl: './book-import-enrichment.component.css',
})
export class BookImportEnrichmentComponent implements OnInit, OnChanges {
  @Input() allRows: BookImportRow[] = [];
  @Input() school!: School;
  @Input() rowErrors: string[] = [];

  @Output() confirmed = new EventEmitter<BookImportRow[]>();
  @Output() cancelled = new EventEmitter<void>();

  enrichableStates: EnrichedRowState[] = [];
  localRows: BookImportRow[] = [];
  errorsByRow: Map<number, string[]> = new Map();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['rowErrors'] && !changes['rowErrors'].firstChange) {
      this.errorsByRow = new Map();
      this.rowErrors.forEach((error) => {
        const match = error.match(/^Rij (\d+): (.+)$/);
        if (match) {
          const rowNum = parseInt(match[1], 10);
          if (!this.errorsByRow.has(rowNum)) this.errorsByRow.set(rowNum, []);
          this.errorsByRow.get(rowNum)!.push(match[2]);
        }
      });

      const existingRowNumbers = new Set(this.enrichableStates.map((s) => s.original.rowNumber));
      for (const row of this.localRows) {
        if (!existingRowNumbers.has(row.rowNumber) && this.errorsByRow.has(row.rowNumber)) {
          this.enrichableStates.push({
            original: row,
            titleInput: row.title,
            authorInput: row.author,
            isbnInput: row.isbn,
            campusInput: row.campus.toLowerCase().replace(/ /g, ''),
            conditionNieuw: row.conditionNieuw,
            conditionGoed: row.conditionGoed,
            conditionGebruikt: row.conditionGebruikt,
            conditionBeschadigd: row.conditionBeschadigd,
            updateExistingIsbn: false,
            validationErrors: [],
            invalidFields: new Set<string>(),
          });
        }
      }
    }
  }

  ngOnInit(): void {
    this.rowErrors.forEach((error) => {
      const match = error.match(/^Rij (\d+): (.+)$/);
      if (match) {
        const rowNum = parseInt(match[1], 10);
        if (!this.errorsByRow.has(rowNum)) {
          this.errorsByRow.set(rowNum, []);
        }
        this.errorsByRow.get(rowNum)!.push(match[2]);
      }
    });

    this.localRows = this.allRows.map((r) => ({ ...r }));

    this.enrichableStates = this.localRows
      .filter((row) => this.rowNeedsEnrichment(row) || this.errorsByRow.has(row.rowNumber))
      .map((row) => ({
        original: row,
        titleInput: row.title,
        authorInput: row.author,
        isbnInput: row.isbn,
        campusInput: row.campus.toLowerCase().replace(/ /g, ''),
        conditionNieuw: row.conditionNieuw,
        conditionGoed: row.conditionGoed,
        conditionGebruikt: row.conditionGebruikt,
        conditionBeschadigd: row.conditionBeschadigd,
        updateExistingIsbn: false,
        validationErrors: [],
        invalidFields: new Set<string>(),
      }));
  }

  hasBackendErrors(rowNumber: number): boolean {
    return this.errorsByRow.has(rowNumber);
  }

  getRowErrors(rowNumber: number): string[] {
    return this.errorsByRow.get(rowNumber) ?? [];
  }

  rowNeedsEnrichment(row: BookImportRow): boolean {
    return !!row.existingIsbn && row.existingIsbn !== row.isbn;
  }

  conditionSum(state: EnrichedRowState): number {
    return (
      (state.conditionNieuw || 0) +
      (state.conditionGoed || 0) +
      (state.conditionGebruikt || 0) +
      (state.conditionBeschadigd || 0)
    );
  }

  onDeleteRow(rowNumber: number): void {
    this.localRows = this.localRows
      .filter((r) => r.rowNumber !== rowNumber)
      .map((r) => (r.rowNumber > rowNumber ? { ...r, rowNumber: r.rowNumber - 1 } : r));

    this.enrichableStates = this.enrichableStates
      .filter((s) => s.original.rowNumber !== rowNumber)
      .map((s) => {
        if (s.original.rowNumber > rowNumber) {
          return { ...s, original: { ...s.original, rowNumber: s.original.rowNumber - 1 } };
        }
        return s;
      });

    const newErrorsByRow = new Map<number, string[]>();
    this.errorsByRow.forEach((errors, key) => {
      if (key === rowNumber) return;
      newErrorsByRow.set(key > rowNumber ? key - 1 : key, errors);
    });
    this.errorsByRow = newErrorsByRow;
  }

  onConfirm(): void {
    this.errorsByRow.clear();

    for (const state of this.enrichableStates) {
      state.validationErrors = [];
      state.invalidFields = new Set<string>();

      if (!state.titleInput.trim()) {
        state.validationErrors.push("'Titel' is verplicht.");
        state.invalidFields.add('title');
      }
      if (!state.authorInput.trim()) {
        state.validationErrors.push("'Auteur' is verplicht.");
        state.invalidFields.add('author');
      }
      if (state.isbnInput.trim() && !/^\d{13}$/.test(state.isbnInput.trim())) {
        state.validationErrors.push("'ISBN' moet exact 13 cijfers bevatten.");
        state.invalidFields.add('isbn');
      }
      if (this.conditionSum(state) < 1) {
        state.validationErrors.push('Som van conditie-aantallen moet minimaal 1 zijn.');
        state.invalidFields.add('conditionSum');
      }
      if (
        state.campusInput &&
        !this.school.campuses.some(
          (c) => c.name.toLowerCase().replace(/ /g, '') === state.campusInput,
        )
      ) {
        state.validationErrors.push(
          `Campus '${state.campusInput}' niet gevonden voor deze school.`,
        );
        state.invalidFields.add('campus');
      }
    }

    if (this.enrichableStates.some((s) => s.validationErrors.length > 0)) return;

    const stateMap = new Map<number, EnrichedRowState>();
    for (const state of this.enrichableStates) {
      stateMap.set(state.original.rowNumber, state);
    }

    const mergedRows: BookImportRow[] = this.localRows.map((row) => {
      const state = stateMap.get(row.rowNumber);
      if (!state) return row;
      return {
        ...row,
        title: state.titleInput,
        author: state.authorInput,
        isbn: state.isbnInput,
        campus: state.campusInput,
        conditionNieuw: state.conditionNieuw || 0,
        conditionGoed: state.conditionGoed || 0,
        conditionGebruikt: state.conditionGebruikt || 0,
        conditionBeschadigd: state.conditionBeschadigd || 0,
        updateExistingIsbn: state.updateExistingIsbn,
      };
    });

    this.confirmed.emit(mergedRows);
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
