// I13 — notificationsReducer + exportReducer (in-memory)
//
// Verifies in-memory Redux reducers that do not touch localStorage.
// Both reducers can be tested without any beforeEach/afterEach setup.

import { describe, it, expect } from 'vitest';
import { configureStore } from '@reduxjs/toolkit';

import NotificationsReducer, { addNotification, removeNotification } from '../../../redux/reducer/general/notificationsReducer.ts';
import ExportReducer, { setExportType, setExportSVGData, setExportName, ExportType } from '../../../redux/reducer/export/exportReducer.ts';
import type { NotificationType } from '../../../types/general/alertType.ts';
import { AlertType } from '../../../types/general/alertType.ts';

describe('I13 — notificationsReducer + exportReducer (in-memory)', () => {
  // ── I13.1 — fresh notifications store ────────────────────────────────────

  it('I13.1 — fresh notifications store has notificationList: [] and currID: 0', () => {
    const store = configureStore({ reducer: { notifications: NotificationsReducer } });
    const state = store.getState().notifications;
    expect(state.notificationList).toEqual([]);
    expect(state.currID).toBe(0);
  });

  // ── I13.2 — addNotification appends with auto-incremented id ─────────────

  it('I13.2 — addNotification assigns auto-incremented id and appends to list', () => {
    const store = configureStore({ reducer: { notifications: NotificationsReducer } });
    const notif: NotificationType = { text: 'Hello', type: AlertType.information };
    store.dispatch(addNotification(notif));

    const { notificationList, currID } = store.getState().notifications;
    expect(notificationList).toHaveLength(1);
    expect(notificationList[0].id).toBe(0);
    expect(notificationList[0].text).toBe('Hello');
    expect(currID).toBe(1);
  });

  // ── I13.3 — removeNotification removes by id ─────────────────────────────

  it('I13.3 — removeNotification removes the matching notification', () => {
    const store = configureStore({ reducer: { notifications: NotificationsReducer } });
    store.dispatch(addNotification({ text: 'First', type: AlertType.information }));
    store.dispatch(addNotification({ text: 'Second', type: AlertType.success }));

    const id = store.getState().notifications.notificationList[0].id!;
    store.dispatch(removeNotification(id));

    expect(store.getState().notifications.notificationList).toHaveLength(1);
    expect(store.getState().notifications.notificationList[0].text).toBe('Second');
  });

  // ── I13.4 — fresh export store ────────────────────────────────────────────

  it('I13.4 — fresh export store has exportType: all and exportName: export', () => {
    const store = configureStore({ reducer: { export: ExportReducer } });
    const state = store.getState().export;
    expect(state.exportType).toBe(ExportType.all);
    expect(state.exportName).toBe('export');
  });

  // ── I13.5 — setExportType updates state ───────────────────────────────────

  it('I13.5 — setExportType(image) updates exportType', () => {
    const store = configureStore({ reducer: { export: ExportReducer } });
    store.dispatch(setExportType(ExportType.image));
    expect(store.getState().export.exportType).toBe(ExportType.image);
  });

  // ── I13.6 — setExportName and setExportSVGData update state ──────────────

  it('I13.6 — setExportName and setExportSVGData update their respective state fields', () => {
    const store = configureStore({ reducer: { export: ExportReducer } });
    store.dispatch(setExportName('myChart'));
    store.dispatch(setExportSVGData('<svg><rect/></svg>'));

    expect(store.getState().export.exportName).toBe('myChart');
    expect(store.getState().export.exportSVGData).toBe('<svg><rect/></svg>');
  });
});
