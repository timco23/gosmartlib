import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { BookImportRow, School } from '../../../types/types';

interface EnrichedRowState {
  original: BookImportRow;
  campusInput: string;
  conditionNieuw: number;
  conditionGoed: number;
  conditionGebruikt: number;
  conditionBeschadigd: number;
  validationError: string;
}

@Component({
  selector: 'app-book-import-enrichment',
  imports: [FormsModule, NgClass],
  templateUrl: './book-import-enrichment.component.html',
  styleUrl: './book-import-enrichment.component.css',
})
export class BookImportEnrichmentComponent implements OnInit {
  @Input() allRows: BookImportRow[] = [];
  @Input() school!: School;

  @Output() confirmed = new EventEmitter<BookImportRow[]>();
  @Output() skipped = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  enrichableStates: EnrichedRowState[] = [];

  ngOnInit(): void {
    this.enrichableStates = this.allRows
      .filter((row) => this.rowNeedsEnrichment(row))
      .map((row) => ({
        original: row,
        campusInput: row.campus,
        conditionNieuw: row.conditionNieuw,
        conditionGoed: row.conditionGoed,
        conditionGebruikt: row.conditionGebruikt,
        conditionBeschadigd: row.conditionBeschadigd,
        validationError: '',
      }));
  }

  rowNeedsEnrichment(row: BookImportRow): boolean {
    const conditionSum =
      row.conditionNieuw + row.conditionGoed + row.conditionGebruikt + row.conditionBeschadigd;
    const missingCampus = row.campus === '' && this.school.campuses.length > 1;
    const missingConditions = conditionSum < row.copies;
    return missingCampus || missingConditions;
  }

  conditionSum(state: EnrichedRowState): number {
    return (
      (state.conditionNieuw || 0) +
      (state.conditionGoed || 0) +
      (state.conditionGebruikt || 0) +
      (state.conditionBeschadigd || 0)
    );
  }

  onConfirm(): void {
    let hasErrors = false;
    for (const state of this.enrichableStates) {
      state.validationError = '';
      if (this.conditionSum(state) > state.original.copies) {
        state.validationError =
          `Som van conditie-aantallen (${this.conditionSum(state)}) overschrijdt exemplaar_aantal (${state.original.copies}).`;
        hasErrors = true;
      }
    }
    if (hasErrors) return;

    const stateMap = new Map<number, EnrichedRowState>();
    for (const state of this.enrichableStates) {
      stateMap.set(state.original.rowNumber, state);
    }

    const mergedRows: BookImportRow[] = this.allRows.map((row) => {
      const state = stateMap.get(row.rowNumber);
      if (!state) return row;
      return {
        ...row,
        campus: state.campusInput,
        conditionNieuw: state.conditionNieuw || 0,
        conditionGoed: state.conditionGoed || 0,
        conditionGebruikt: state.conditionGebruikt || 0,
        conditionBeschadigd: state.conditionBeschadigd || 0,
      };
    });

    this.confirmed.emit(mergedRows);
  }

  onSkip(): void {
    this.skipped.emit();
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
