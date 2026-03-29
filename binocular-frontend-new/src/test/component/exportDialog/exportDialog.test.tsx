import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import ExportDialog from '../../../components/exportDialog/exportDialog';
import ExportReducer, { ExportType } from '../../../redux/reducer/export/exportReducer';

function makeStore(exportType: ExportType = ExportType.all, exportSVGData = '<svg></svg>', exportName = 'export') {
  return configureStore({
    reducer: { export: ExportReducer },
    preloadedState: { export: { exportType, exportSVGData, exportName } },
  });
}

function renderDialog(exportType: ExportType = ExportType.all) {
  return render(
    <Provider store={makeStore(exportType)}>
      <ExportDialog />
    </Provider>,
  );
}

describe('ExportDialog', () => {
  it('C26.1 renders dialog with id="exportDialog"', () => {
    renderDialog();
    expect(document.getElementById('exportDialog')).not.toBeNull();
  });

  it('C26.2 shows heading "Export" for ExportType.all', () => {
    const { getByText } = renderDialog(ExportType.all);
    expect(getByText('Export')).not.toBeNull();
  });

  it('C26.3 shows heading "Image Export" for ExportType.image', () => {
    const { getByText } = renderDialog(ExportType.image);
    expect(getByText('Image Export')).not.toBeNull();
  });

  it('C26.4 shows heading "Data Export" for ExportType.data', () => {
    const { getByText } = renderDialog(ExportType.data);
    expect(getByText('Data Export')).not.toBeNull();
  });

  it('C26.5 shows "Export SVG" button for ExportType.image', () => {
    const { getByText } = renderDialog(ExportType.image);
    expect(getByText('Export SVG')).not.toBeNull();
  });

  it('C26.6 no "Export SVG" button for ExportType.all', () => {
    const { queryByText } = renderDialog(ExportType.all);
    expect(queryByText('Export SVG')).toBeNull();
  });

  it('C26.7 always renders a "Close" button', () => {
    const { getAllByText } = renderDialog(ExportType.all);
    expect(getAllByText('Close').length).toBeGreaterThan(0);
  });
});
