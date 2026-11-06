import { describe, it, expect } from 'vitest';
import reducer, { setExportType, setExportSVGData, setExportName, ExportType } from '../../../../../redux/reducer/export/exportReducer';
import type { ExportInitialState } from '../../../../../redux/reducer/export/exportReducer';

const initialState: ExportInitialState = {
  exportType: ExportType.all,
  exportSVGData: '<svg></svg>',
  exportName: 'export',
};

describe('exportReducer – setExportType', () => {
  it('U19.1 updates exportType', () => {
    const state = reducer(initialState, setExportType(ExportType.image));
    expect(state.exportType).toBe(ExportType.image);
  });

  it('U19.2 overwrites a previous value', () => {
    const after1 = reducer(initialState, setExportType(ExportType.image));
    const after2 = reducer(after1, setExportType(ExportType.data));
    expect(after2.exportType).toBe(ExportType.data);
  });
});

describe('exportReducer – setExportSVGData', () => {
  it('U19.3 stores an SVG string', () => {
    const svg = '<svg><rect width="100" height="100"/></svg>';
    const state = reducer(initialState, setExportSVGData(svg));
    expect(state.exportSVGData).toBe(svg);
  });
});

describe('exportReducer – setExportName', () => {
  it('U19.4 sets the filename', () => {
    const state = reducer(initialState, setExportName('my-chart'));
    expect(state.exportName).toBe('my-chart');
  });

  it('U19.5 overwrites a previous name', () => {
    const after1 = reducer(initialState, setExportName('first'));
    const after2 = reducer(after1, setExportName('second'));
    expect(after2.exportName).toBe('second');
  });
});
