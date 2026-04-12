import { describe, it, expect, beforeEach, vi } from 'vitest';
import { clearHighlightDropArea, setDragResizeMode, placeDragIndicator } from '../../../../components/dashboard/dashboardHelper';
import { DragResizeMode } from '../../../../components/dashboard/resizeMode';

// dashboardHelper imports a CSS module — mock it to avoid Vite transform issues in unit context
vi.mock('../../../../components/dashboard/dashboard.module.scss', () => ({
  default: {
    dashboardBackgroundCellHighlightActive: 'highlightActive',
    dashboardBackgroundCellHighlightNotPossible: 'highlightNotPossible',
  },
}));

function makeDivRef(el: HTMLDivElement | null = document.createElement('div')) {
  return { current: el };
}

function makeModeRef(mode: DragResizeMode = DragResizeMode.none) {
  return { current: mode };
}

// ──────────────────────────────────────────────────────────────────────────────
// clearHighlightDropArea
// ──────────────────────────────────────────────────────────────────────────────
describe('clearHighlightDropArea', () => {
  it('U53.1 hides drag indicator (display none)', () => {
    const div = document.createElement('div');
    div.style.display = 'block';
    clearHighlightDropArea(makeDivRef(div), 0, 0);
    expect(div.style.display).toBe('none');
  });

  it('U53.2 removes highlight classes from grid cells', () => {
    document.body.innerHTML = `
      <div id="highlightY0X0" class="highlightActive"></div>
      <div id="highlightY0X1" class="highlightNotPossible"></div>
    `;
    // clearHighlightDropArea(ref, columnCount, rowCount) — 2 columns, 1 row
    clearHighlightDropArea(makeDivRef(), 2, 1);
    expect(document.getElementById('highlightY0X0')!.classList.contains('highlightActive')).toBe(false);
    expect(document.getElementById('highlightY0X1')!.classList.contains('highlightNotPossible')).toBe(false);
  });

  it('U53.3 no-op when ref.current is null', () => {
    expect(() => clearHighlightDropArea(makeDivRef(null), 0, 0)).not.toThrow();
  });
});

// ──────────────────────────────────────────────────────────────────────────────
// setDragResizeMode
// ──────────────────────────────────────────────────────────────────────────────
describe('setDragResizeMode', () => {
  beforeEach(() => {
    document.body.innerHTML = '';
  });

  it('U53.4 sets dragResizeMode.current to the new value', () => {
    const zone = document.createElement('div');
    const zoneRef = makeDivRef(zone);
    const modeRef = makeModeRef(DragResizeMode.none);
    setDragResizeMode(zoneRef, modeRef, DragResizeMode.drag);
    expect(modeRef.current).toBe(DragResizeMode.drag);
  });

  it('U53.5 shows div when mode is non-none', () => {
    const zone = document.createElement('div');
    const zoneRef = makeDivRef(zone);
    const modeRef = makeModeRef(DragResizeMode.none);
    setDragResizeMode(zoneRef, modeRef, DragResizeMode.drag);
    expect(zone.style.display).toBe('block');
  });

  it('U53.6 hides div when mode is none', () => {
    const zone = document.createElement('div');
    zone.style.display = 'block';
    const zoneRef = makeDivRef(zone);
    const modeRef = makeModeRef(DragResizeMode.drag);
    setDragResizeMode(zoneRef, modeRef, DragResizeMode.none);
    expect(zone.style.display).toBe('none');
  });
});

// ──────────────────────────────────────────────────────────────────────────────
// placeDragIndicator
// ──────────────────────────────────────────────────────────────────────────────
describe('placeDragIndicator', () => {
  it('U53.7 sets display block and calc-based style properties', () => {
    const div = document.createElement('div');
    const divRef = makeDivRef(div);
    const movingItem = { current: { x: 2, y: 1, width: 4, height: 3, id: 1 } };
    placeDragIndicator(divRef, movingItem as never, 4, 2, 3);
    expect(div.style.display).toBe('block');
    expect(div.style.top).toContain('calc(');
    expect(div.style.left).toContain('calc(');
    expect(div.style.width).toContain('calc(');
    expect(div.style.height).toContain('calc(');
  });

  it('U53.8 no-op when ref.current is null', () => {
    const movingItem = { current: { x: 0, y: 0, width: 1, height: 1, id: 1 } };
    expect(() => placeDragIndicator(makeDivRef(null), movingItem as never, 4, 2, 3)).not.toThrow();
  });
});
