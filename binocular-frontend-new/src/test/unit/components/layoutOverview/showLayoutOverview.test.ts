import { describe, it, expect, beforeEach, vi } from 'vitest';
import { showLayoutOverview } from '../../../../components/tabs/layouts/layoutOverview/layoutOverviewHelper';

const W = 1000;
const H = 800;

beforeEach(() => {
  document.body.innerHTML = `
    <dialog id="layoutOverview"></dialog>
    <div id="layoutOverviewPositionController" style=""></div>
  `;
  HTMLDialogElement.prototype.showModal = vi.fn();
  Object.defineProperty(window, 'innerWidth', { value: W, writable: true, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: H, writable: true, configurable: true });
});

function getController() {
  return document.getElementById('layoutOverviewPositionController') as HTMLDivElement;
}

describe('showLayoutOverview – vertical positioning', () => {
  it('U47.1 y < innerHeight/2 → top set, bottom: auto', () => {
    showLayoutOverview(500, 200);
    const ctrl = getController();
    expect(ctrl.style.top).toBe('180px');
    expect(ctrl.style.bottom).toBe('auto');
  });

  it('U47.2 y >= innerHeight/2 → bottom set, top: auto', () => {
    showLayoutOverview(500, 600);
    const ctrl = getController();
    expect(ctrl.style.bottom).toBe('180px');
    expect(ctrl.style.top).toBe('auto');
  });

  it('U47.5 y=20 edge case: y-20=0 < 10 → top clamped to 10px', () => {
    showLayoutOverview(500, 20);
    const ctrl = getController();
    expect(ctrl.style.top).toBe('10px');
  });
});

describe('showLayoutOverview – horizontal positioning', () => {
  it('U47.3 x < innerWidth/2 → left set, right: auto', () => {
    showLayoutOverview(200, 100);
    const ctrl = getController();
    expect(ctrl.style.left).toBe('200px');
    expect(ctrl.style.right).toBe('auto');
  });

  it('U47.4 x >= innerWidth/2 → right set, left: auto', () => {
    showLayoutOverview(700, 100);
    const ctrl = getController();
    expect(ctrl.style.right).toBe('280px');
    expect(ctrl.style.left).toBe('auto');
  });
});

describe('showLayoutOverview – showModal', () => {
  it('U47.6 calls showModal() on #layoutOverview', () => {
    showLayoutOverview(100, 100);
    const dialog = document.getElementById('layoutOverview') as HTMLDialogElement;
    expect(dialog.showModal).toHaveBeenCalledOnce();
  });
});
