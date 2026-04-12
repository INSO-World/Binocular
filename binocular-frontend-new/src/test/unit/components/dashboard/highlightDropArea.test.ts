import { describe, it, expect } from 'vitest';
import { highlightDropArea } from '../../../../components/dashboard/dashboardHelper';
import type { DashboardItemDTO } from '../../../../types/general/dashboardItemType';
import type { MutableRefObject } from 'react';

function makeMovingItemRef(id: number): MutableRefObject<DashboardItemDTO> {
  return { current: { id, x: 0, y: 0, width: 2, height: 2 } };
}

/** Build an N×N grid of zeros, then optionally mark specific cells */
function makeGrid(size: number, occupied: { y: number; x: number; id: number }[] = []): number[][] {
  const grid = Array.from({ length: size }, () => Array(size).fill(0));
  for (const { y, x, id } of occupied) {
    grid[y][x] = id;
  }
  return grid;
}

describe('highlightDropArea – placeable return value', () => {
  it('U30.1 empty grid returns true', () => {
    const movingItem = makeMovingItemRef(1);
    const state = makeGrid(4);
    expect(highlightDropArea(movingItem, state, 4, 4, 1, 0, 0, 2, 2)).toBe(true);
  });

  it('U30.2 cell occupied by the moving item itself returns true', () => {
    const movingItem = makeMovingItemRef(1);
    const state = makeGrid(4, [{ y: 0, x: 0, id: 1 }]);
    expect(highlightDropArea(movingItem, state, 4, 4, 1, 0, 0, 2, 2)).toBe(true);
  });

  it('U30.3 cell occupied by a different item returns false', () => {
    const movingItem = makeMovingItemRef(1);
    const state = makeGrid(4, [{ y: 1, x: 1, id: 2 }]);
    expect(highlightDropArea(movingItem, state, 4, 4, 1, 0, 0, 2, 2)).toBe(false);
  });

  it('U30.4 occupied cell outside the drop area returns true', () => {
    const movingItem = makeMovingItemRef(1);
    // cell (3,3) is outside a 2×2 area starting at (0,0)
    const state = makeGrid(4, [{ y: 3, x: 3, id: 2 }]);
    expect(highlightDropArea(movingItem, state, 4, 4, 1, 0, 0, 2, 2)).toBe(true);
  });

  it('U30.5 multiple conflicting cells return false', () => {
    const movingItem = makeMovingItemRef(1);
    const state = makeGrid(4, [
      { y: 0, x: 0, id: 2 },
      { y: 1, x: 1, id: 3 },
    ]);
    expect(highlightDropArea(movingItem, state, 4, 4, 1, 0, 0, 2, 2)).toBe(false);
  });
});
