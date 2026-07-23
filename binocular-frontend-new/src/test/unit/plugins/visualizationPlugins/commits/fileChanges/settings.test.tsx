import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { act } from 'react';

import Settings, { type SettingsType } from '../../../../../../plugins/visualizationPlugins/commits/fileChanges/src/settings/settings.tsx';
import { changesSlice, setFiles } from '../../../../../../plugins/visualizationPlugins/commits/fileChanges/src/reducer';
import type { FileListElementType } from '../../../../../../types/data/fileListType';

function makeFile(path: string): FileListElementType {
  return { element: { path, webUrl: '', maxLength: 0 }, checked: true };
}

function createTestStore() {
  return configureStore({ reducer: { plugin: changesSlice.reducer } });
}

const defaultSettings: SettingsType = {
  file: '',
  splitAdditionsDeletions: false,
  visualizationStyle: 'curved',
  showSprints: false,
  showExtraMetrics: false,
};

describe('fileChanges Settings', () => {
  it('shows the file list immediately when the store already has files at mount time', () => {
    const store = createTestStore();
    store.dispatch(setFiles([makeFile('a.ts'), makeFile('b.ts')]));

    render(<Settings settings={defaultSettings} setSettings={() => {}} store={store} />);

    expect(screen.queryByText('No files found. Load File Tree first.')).not.toBeInTheDocument();
    expect(screen.getByText('a.ts')).toBeInTheDocument();
    expect(screen.getByText('b.ts')).toBeInTheDocument();
  });

  it('shows "No files found" when the store has no files yet', () => {
    const store = createTestStore();

    render(<Settings settings={defaultSettings} setSettings={() => {}} store={store} />);

    expect(screen.getByText('No files found. Load File Tree first.')).toBeInTheDocument();
  });

  it('stays in sync when files are dispatched after mount', () => {
    const store = createTestStore();

    render(<Settings settings={defaultSettings} setSettings={() => {}} store={store} />);
    expect(screen.getByText('No files found. Load File Tree first.')).toBeInTheDocument();

    act(() => {
      store.dispatch(setFiles([makeFile('c.ts')]));
    });

    expect(screen.getByText('c.ts')).toBeInTheDocument();
  });
});
