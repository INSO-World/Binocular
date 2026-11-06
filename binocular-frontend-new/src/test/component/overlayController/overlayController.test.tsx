import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';

vi.mock('../../../components/informationDialog/informationDialog.tsx', () => ({
  default: () => <div data-testid="mock-information-dialog" />,
}));
vi.mock('../../../components/exportDialog/exportDialog.tsx', () => ({ default: () => <div data-testid="mock-export-dialog" /> }));
vi.mock('../../../components/settingsDialog/settingsDialog.tsx', () => ({ default: () => <div data-testid="mock-settings-dialog" /> }));
vi.mock('../../../components/setupDialog/setupDialog.tsx', () => ({ default: () => <div data-testid="mock-setup-dialog" /> }));
vi.mock('../../../components/notificationController/notificationController.tsx', () => ({
  default: () => <div data-testid="mock-notification-controller" />,
}));
vi.mock('../../../components/tabs/authors/editAuthorDialog/editAuthorDialog.tsx', () => ({
  default: () => <div data-testid="mock-edit-author-dialog" />,
}));
vi.mock('../../../components/fileTree/fileTreeElementInfoDialog/fileTreeElementInfoDialog.tsx', () => ({
  default: () => <div data-testid="mock-file-tree-info-dialog" />,
}));
vi.mock('../../../components/tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverview.tsx', () => ({
  default: () => <div data-testid="mock-visualization-overview" />,
}));
vi.mock('../../../components/tabs/layouts/layoutOverview/layoutOverview.tsx', () => ({
  default: () => <div data-testid="mock-layout-overview" />,
}));
vi.mock('../../../components/overlayController/overlays/loadingLocalDatabaseOverlay/loadingLocalDatabaseOverlay.tsx', () => ({
  default: () => <div data-testid="mock-loading-overlay" />,
}));
vi.mock('../../../components/contextMenu/contextMenu.tsx', () => ({ default: () => <div data-testid="mock-context-menu" /> }));

import OverlayController from '../../../components/overlayController/overlayController.tsx';

describe('OverlayController', () => {
  it('C40.1 renders without crashing', () => {
    render(<OverlayController />);
    expect(document.body.firstChild).not.toBeNull();
  });

  it('C40.2 all 11 overlay child components are present in the DOM', () => {
    render(<OverlayController />);
    const expectedTestIds = [
      'mock-information-dialog',
      'mock-export-dialog',
      'mock-settings-dialog',
      'mock-setup-dialog',
      'mock-notification-controller',
      'mock-edit-author-dialog',
      'mock-file-tree-info-dialog',
      'mock-visualization-overview',
      'mock-layout-overview',
      'mock-loading-overlay',
      'mock-context-menu',
    ];
    for (const testId of expectedTestIds) {
      expect(screen.getByTestId(testId)).toBeInTheDocument();
    }
  });
});
