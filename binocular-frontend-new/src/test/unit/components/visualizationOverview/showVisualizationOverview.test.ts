import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  showVisualizationOverview,
  disableVisualizationOverview,
} from '../../../../components/tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverviewHelper';
import type { VisualizationPluginCompatibility } from '../../../../plugins/interfaces/visualizationPluginInterfaces/visualizationPluginMetadata';

const W = 1000;
const H = 800;

beforeEach(() => {
  document.body.innerHTML = `
    <dialog id="visualizationOverview"></dialog>
    <div id="visualizationOverviewPositionController" style=""></div>
  `;
  HTMLDialogElement.prototype.showModal = vi.fn();
  Object.defineProperty(window, 'innerWidth', { value: W, writable: true, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: H, writable: true, configurable: true });
});

function getController() {
  return document.getElementById('visualizationOverviewPositionController') as HTMLDivElement;
}

const allFalse: VisualizationPluginCompatibility = {
  binocularBackend: false,
  githubAPI: false,
  mockData: false,
  pouchDB: false,
  github: false,
  gitlab: false,
};

describe('showVisualizationOverview – vertical positioning', () => {
  it('U48.1 y < innerHeight/2 → top set, bottom: auto', () => {
    showVisualizationOverview(500, 200);
    const ctrl = getController();
    expect(ctrl.style.top).toBe('180px');
    expect(ctrl.style.bottom).toBe('auto');
  });

  it('U48.2 y >= innerHeight/2 → bottom set, top: auto', () => {
    showVisualizationOverview(500, 600);
    const ctrl = getController();
    expect(ctrl.style.bottom).toBe('180px');
    expect(ctrl.style.top).toBe('auto');
  });
});

describe('showVisualizationOverview – horizontal positioning', () => {
  it('U48.3 x < innerWidth/2 → left set, right: auto', () => {
    showVisualizationOverview(200, 100);
    const ctrl = getController();
    expect(ctrl.style.left).toBe('200px');
    expect(ctrl.style.right).toBe('auto');
  });

  it('U48.4 x >= innerWidth/2 → right set, left: auto', () => {
    showVisualizationOverview(700, 100);
    const ctrl = getController();
    expect(ctrl.style.right).toBe('280px');
    expect(ctrl.style.left).toBe('auto');
  });
});

describe('showVisualizationOverview – showModal', () => {
  it('U48.5 calls showModal() on #visualizationOverview', () => {
    showVisualizationOverview(100, 100);
    const dialog = document.getElementById('visualizationOverview') as HTMLDialogElement;
    expect(dialog.showModal).toHaveBeenCalledOnce();
  });
});

describe('disableVisualizationOverview', () => {
  it('U48.6 returns false when pluginOptions is undefined', () => {
    expect(disableVisualizationOverview(allFalse, undefined)).toBe(false);
  });

  it('U48.7 returns false when no filter key is true', () => {
    const plugin: VisualizationPluginCompatibility = { ...allFalse };
    expect(disableVisualizationOverview(allFalse, plugin)).toBe(false);
  });

  it('U48.8 returns true when filter github=true but plugin github=false', () => {
    const filter: VisualizationPluginCompatibility = { ...allFalse, github: true };
    const plugin: VisualizationPluginCompatibility = { ...allFalse, github: false };
    expect(disableVisualizationOverview(filter, plugin)).toBe(true);
  });

  it('U48.9 returns false when filter github=true and plugin github=true', () => {
    const filter: VisualizationPluginCompatibility = { ...allFalse, github: true };
    const plugin: VisualizationPluginCompatibility = { ...allFalse, github: true };
    expect(disableVisualizationOverview(filter, plugin)).toBe(false);
  });

  it('U48.10 returns true on pouchDB key mismatch', () => {
    const filter: VisualizationPluginCompatibility = { ...allFalse, pouchDB: true };
    const plugin: VisualizationPluginCompatibility = { ...allFalse, pouchDB: false };
    expect(disableVisualizationOverview(filter, plugin)).toBe(true);
  });
});
