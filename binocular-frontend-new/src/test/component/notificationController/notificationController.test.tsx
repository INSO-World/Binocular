import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import NotificationController from '../../../components/notificationController/notificationController.tsx';
import NotificationsReducer, { addNotification } from '../../../redux/reducer/general/notificationsReducer.ts';
import { AlertType, type NotificationType } from '../../../types/general/alertType.ts';

function createStore(initialNotifications: NotificationType[] = []) {
  const store = configureStore({
    reducer: {
      notifications: NotificationsReducer,
    },
    preloadedState: {
      notifications: {
        notificationList: initialNotifications,
        currID: initialNotifications.length,
      },
    },
  });
  return store;
}

function renderWithStore(store: ReturnType<typeof createStore>) {
  return render(
    <Provider store={store}>
      <NotificationController />
    </Provider>,
  );
}

describe('NotificationController', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('C2.1 renders nothing when notification list is empty', () => {
    const store = createStore([]);
    renderWithStore(store);
    expect(screen.queryAllByRole('alert')).toHaveLength(0);
  });

  it('C2.2 renders one toast per notification (2 notifications → 2 elements)', () => {
    const store = createStore([
      { id: 0, text: 'First', type: AlertType.information },
      { id: 1, text: 'Second', type: AlertType.error },
    ]);
    renderWithStore(store);
    expect(screen.getAllByRole('alert')).toHaveLength(2);
  });

  it('C2.3 displays notification text', () => {
    const store = createStore([{ id: 0, text: 'Build failed', type: AlertType.error }]);
    renderWithStore(store);
    expect(screen.getByText('Build failed')).toBeInTheDocument();
  });

  it('C2.4 renders correct indicator for error type (alert-error class)', () => {
    const store = createStore([{ id: 0, text: 'Error!', type: AlertType.error }]);
    renderWithStore(store);
    const alert = screen.getByRole('alert');
    expect(alert.className).toContain('alert-error');
  });

  it('C2.5 renders correct indicator for success type (alert-success class)', () => {
    const store = createStore([{ id: 0, text: 'Success!', type: AlertType.success }]);
    renderWithStore(store);
    const alert = screen.getByRole('alert');
    expect(alert.className).toContain('alert-success');
  });

  it('C2.6 renders correct indicator for warning type (alert-warning class)', () => {
    const store = createStore([{ id: 0, text: 'Warning!', type: AlertType.warning }]);
    renderWithStore(store);
    const alert = screen.getByRole('alert');
    expect(alert.className).toContain('alert-warning');
  });

  it('C2.7 renders correct indicator for info type (alert-info class)', () => {
    const store = createStore([{ id: 0, text: 'Info!', type: AlertType.information }]);
    renderWithStore(store);
    const alert = screen.getByRole('alert');
    expect(alert.className).toContain('alert-info');
  });

  it('C2.8 clicking a toast dispatches removeNotification (notification removed from store)', () => {
    vi.useFakeTimers();
    const store = createStore([{ id: 0, text: 'Click me', type: AlertType.information }]);
    renderWithStore(store);
    const alert = screen.getByRole('alert');
    fireEvent.click(alert);
    // After clicking, a 1000ms setTimeout fires the dispatch
    act(() => {
      vi.advanceTimersByTime(1100);
    });
    expect(store.getState().notifications.notificationList).toHaveLength(0);
  });

  it('C2.9 auto-dismisses after 10 seconds', () => {
    vi.useFakeTimers();
    const store = createStore();
    // Add notification after render so the setTimeout is set up
    const { rerender } = render(
      <Provider store={store}>
        <NotificationController />
      </Provider>,
    );
    act(() => {
      store.dispatch(addNotification({ text: 'Auto dismiss', type: AlertType.information }));
    });
    rerender(
      <Provider store={store}>
        <NotificationController />
      </Provider>,
    );
    expect(screen.getAllByRole('alert')).toHaveLength(1);

    // Advance past 10s outer timeout + 1s inner timeout
    act(() => {
      vi.advanceTimersByTime(11100);
    });
    expect(store.getState().notifications.notificationList).toHaveLength(0);
  });

  it('C2.10 multiple notifications dismissed independently', () => {
    vi.useFakeTimers();
    const store = createStore([
      { id: 0, text: 'First', type: AlertType.information },
      { id: 1, text: 'Second', type: AlertType.error },
    ]);
    renderWithStore(store);

    const alerts = screen.getAllByRole('alert');
    // Click the first rendered alert (reversed order, so second notification is first in DOM)
    fireEvent.click(alerts[0]);

    act(() => {
      vi.advanceTimersByTime(1100);
    });

    // One should be removed, one should remain
    expect(store.getState().notifications.notificationList).toHaveLength(1);
  });
});
