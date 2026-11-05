import { describe, it, expect } from 'vitest';
import reducer, { addNotification, removeNotification } from '../../../../../redux/reducer/general/notificationsReducer';
import { AlertType } from '../../../../../types/general/alertType';
import type { NotificationsInitialState } from '../../../../../redux/reducer/general/notificationsReducer';

const empty: NotificationsInitialState = { notificationList: [], currID: 0 };

describe('notificationsReducer – addNotification', () => {
  it('U16.1 appends a notification to the list', () => {
    const state = reducer(empty, addNotification({ text: 'hello', type: AlertType.information }));
    expect(state.notificationList).toHaveLength(1);
    expect(state.notificationList[0].text).toBe('hello');
  });

  it('U16.2 assigns the current currID as the notification id', () => {
    const state = reducer({ ...empty, currID: 5 }, addNotification({ text: 'x', type: AlertType.warning }));
    expect(state.notificationList[0].id).toBe(5);
  });

  it('U16.3 increments currID after each add', () => {
    let state = reducer(empty, addNotification({ text: 'a', type: AlertType.success }));
    state = reducer(state, addNotification({ text: 'b', type: AlertType.error }));
    expect(state.currID).toBe(2);
  });

  it('U16.4 accumulates multiple notifications', () => {
    let state = reducer(empty, addNotification({ text: '1', type: AlertType.information }));
    state = reducer(state, addNotification({ text: '2', type: AlertType.information }));
    state = reducer(state, addNotification({ text: '3', type: AlertType.information }));
    expect(state.notificationList).toHaveLength(3);
  });
});

describe('notificationsReducer – removeNotification', () => {
  it('U16.5 removes the notification with the matching id', () => {
    const withOne = reducer(empty, addNotification({ text: 'to-remove', type: AlertType.information }));
    const id = withOne.notificationList[0].id!;
    const state = reducer(withOne, removeNotification(id));
    expect(state.notificationList).toHaveLength(0);
  });

  it('U16.6 does not remove other notifications', () => {
    let state = reducer(empty, addNotification({ text: 'keep', type: AlertType.information }));
    state = reducer(state, addNotification({ text: 'remove', type: AlertType.warning }));
    const idToRemove = state.notificationList.find((n) => n.text === 'remove')!.id!;
    state = reducer(state, removeNotification(idToRemove));
    expect(state.notificationList).toHaveLength(1);
    expect(state.notificationList[0].text).toBe('keep');
  });

  it('U16.7 is a no-op when the id does not exist', () => {
    const state = reducer(empty, removeNotification(99));
    expect(state.notificationList).toHaveLength(0);
  });
});
