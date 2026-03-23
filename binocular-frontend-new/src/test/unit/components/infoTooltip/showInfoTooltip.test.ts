import { describe, it, expect, beforeEach, vi } from 'vitest';
import { showInfoTooltip } from '../../../../components/infoTooltip/infoTooltipHelper';
import type { VisualizationPluginCompatibility } from '../../../../plugins/interfaces/visualizationPluginInterfaces/visualizationPluginMetadata';

const W = 1000;
const H = 800;

beforeEach(() => {
  document.body.innerHTML = `
    <dialog id="infoTooltip"></dialog>
    <div id="infoTooltipPositionController" style=""></div>
    <div id="infoTooltipContent"></div>
  `;
  HTMLDialogElement.prototype.showModal = vi.fn();
  Object.defineProperty(window, 'innerWidth', { value: W, writable: true, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: H, writable: true, configurable: true });
});

function getController() {
  return document.getElementById('infoTooltipPositionController') as HTMLDivElement;
}

function getContent() {
  return document.getElementById('infoTooltipContent') as HTMLDivElement;
}

const basicContent = { headline: 'Overview', text: 'Shows commits over time.' };

const fullCompatibility: VisualizationPluginCompatibility = {
  github: true,
  gitlab: false,
  binocularBackend: true,
  pouchDB: false,
  mockData: true,
  githubAPI: false,
};

describe('showInfoTooltip – vertical positioning', () => {
  it('U32.1 y < innerHeight/2 → top set, bottom auto', () => {
    showInfoTooltip(100, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.top).toBe('90px');
    expect(ctrl.style.bottom).toBe('auto');
  });

  it('U32.2 y >= innerHeight/2 → bottom set, top auto', () => {
    showInfoTooltip(100, 600, basicContent);
    const ctrl = getController();
    expect(ctrl.style.bottom).toBe(`${H - 600 - 10}px`);
    expect(ctrl.style.top).toBe('auto');
  });
});

describe('showInfoTooltip – horizontal positioning', () => {
  it('U32.3 x < innerWidth/2 → left set, right auto', () => {
    showInfoTooltip(200, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.left).toBe('190px');
    expect(ctrl.style.right).toBe('auto');
  });

  it('U32.4 x >= innerWidth/2 → right set, left auto', () => {
    showInfoTooltip(700, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.right).toBe(`${W - 700 - 10}px`);
    expect(ctrl.style.left).toBe('auto');
  });
});

describe('showInfoTooltip – content rendering', () => {
  it('U32.5 renders <h1> with correct headline', () => {
    showInfoTooltip(100, 100, basicContent);
    const h1 = getContent().querySelector('h1') as HTMLHeadingElement;
    expect(h1).not.toBeNull();
    expect(h1.innerText).toBe('Overview');
  });

  it('U32.6 renders <p> with correct body text', () => {
    showInfoTooltip(100, 100, basicContent);
    const p = getContent().querySelector('p') as HTMLParagraphElement;
    expect(p).not.toBeNull();
    expect(p.innerText).toBe('Shows commits over time.');
  });

  it('U32.7 no compatibility section when compatibilityInfo is omitted', () => {
    showInfoTooltip(100, 100, basicContent);
    expect(getContent().querySelector('#compatibility')).toBeNull();
  });

  it('U32.8 compatibility section rendered when arg provided', () => {
    showInfoTooltip(100, 100, basicContent, fullCompatibility);
    expect(getContent().querySelector('#compatibility')).not.toBeNull();
  });

  it('U32.9 github: true shows "yes" for GitHub', () => {
    showInfoTooltip(100, 100, basicContent, { ...fullCompatibility, github: true });
    expect(getContent().innerHTML).toContain('GitHub: yes');
  });

  it('U32.10 pouchDB: false shows "no" for PouchDB', () => {
    showInfoTooltip(100, 100, basicContent, { ...fullCompatibility, pouchDB: false });
    expect(getContent().innerHTML).toContain('PouchDB: no');
  });

  it('U32.11 calls showModal() on the dialog', () => {
    showInfoTooltip(100, 100, basicContent);
    const dialog = document.getElementById('infoTooltip') as HTMLDialogElement;
    expect(dialog.showModal).toHaveBeenCalledOnce();
  });
});
