export function lendingStatus(l: { returned: boolean; returnDate: string }): 'returned' | 'overdue' | 'active' {
  if (l.returned) return 'returned';
  if (new Date() > new Date(l.returnDate)) return 'overdue';
  return 'active';
}

export function lendingStatusLabel(l: { returned: boolean; returnDate: string }): string {
  const s = lendingStatus(l);
  if (s === 'returned') return 'Teruggebracht';
  if (s === 'overdue') return 'Te laat';
  return 'Actief';
}

export function formatDate(dateStr: string | null): string {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('nl-BE', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

export function formatChartLabel(key: string): string {
  const parts = key.split('-');
  if (parts.length !== 2) return key;
  const [a, b] = parts;
  if (b.length === 4) return key; // schooljaar YYYY-YYYY
  return new Date(+a, +b - 1).toLocaleDateString('nl-BE', { month: 'short', year: '2-digit' });
}

export function readingLevelBars(distribution: Record<string, number>): { label: string; count: number; pct: number }[] {
  const levels = ['A', 'B', 'C', 'D'];
  const bars = levels.map(label => ({ label, count: distribution[label] ?? 0, pct: 0 }));
  const max = Math.max(...bars.map(b => b.count), 1);
  for (const bar of bars) {
    bar.pct = Math.round((bar.count / max) * 100);
  }
  return bars;
}
