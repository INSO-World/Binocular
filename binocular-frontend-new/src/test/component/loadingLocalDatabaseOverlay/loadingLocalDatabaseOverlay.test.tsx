import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import LoadingLocalDatabaseOverlay from '../../../components/overlayController/overlays/loadingLocalDatabaseOverlay/loadingLocalDatabaseOverlay';
import SettingsReducer, { LocalDatabaseLoadingState } from '../../../redux/reducer/settings/settingsReducer';

function makeStore(loadingState: LocalDatabaseLoadingState) {
  return configureStore({
    reducer: { settings: SettingsReducer },
    preloadedState: {
      settings: {
        general: { gridSize: 1 },
        initialized: false,
        database: { currID: 0, dataPlugins: [] },
        localDatabaseLoadingState: loadingState,
        localDatabaseLoadingMessage: '',
      },
    },
  });
}

describe('LoadingLocalDatabaseOverlay', () => {
  it('C20.1 renders nothing when state is not loading', () => {
    const store = makeStore(LocalDatabaseLoadingState.none);
    render(
      <Provider store={store}>
        <LoadingLocalDatabaseOverlay />
      </Provider>,
    );
    expect(document.querySelector('dialog')).toBeNull();
  });

  it('C20.2 renders an open modal when state is loading', () => {
    const store = makeStore(LocalDatabaseLoadingState.loading);
    render(
      <Provider store={store}>
        <LoadingLocalDatabaseOverlay />
      </Provider>,
    );
    const dialog = document.querySelector('dialog');
    expect(dialog).not.toBeNull();
    expect(dialog!.hasAttribute('open')).toBe(true);
  });

  it('C20.3 modal contains "Loading Local Database" text', () => {
    const store = makeStore(LocalDatabaseLoadingState.loading);
    const { getByText } = render(
      <Provider store={store}>
        <LoadingLocalDatabaseOverlay />
      </Provider>,
    );
    expect(getByText('Loading Local Database')).not.toBeNull();
  });
});
