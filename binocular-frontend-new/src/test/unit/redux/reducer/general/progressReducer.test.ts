import { describe, it, expect } from 'vitest';
import reducer, { setProgress, setConnectionStatus } from '../../../../../redux/reducer/general/progressReducer';
import { SocketConnectionStatusType } from '../../../../../types/general/socketConnectionType';
import type { ProgressType } from '../../../../../types/general/progressType';

const emptyReport: ProgressType = {
  type: '',
  report: {
    commits: { processed: 0, total: 0 },
    issues: { processed: 0, total: 0 },
    builds: { processed: 0, total: 0 },
    files: { processed: 0, total: 0 },
    modules: { processed: 0, total: 0 },
    milestones: { processed: 0, total: 0 },
    mergeRequests: { processed: 0, total: 0 },
  },
};

describe('progressReducer – initial state', () => {
  it('U42.1 initial state has progress.type === empty string', () => {
    const state = reducer(undefined, { type: '@@INIT' });
    expect(state.progress?.type).toBe('');
  });

  it('U42.2 initial socketConnection.status is Idle', () => {
    const state = reducer(undefined, { type: '@@INIT' });
    expect(state.socketConnection.status).toBe(SocketConnectionStatusType.Idle);
  });
});

describe('progressReducer – setProgress', () => {
  it('U42.3 setProgress replaces entire progress object', () => {
    const payload: ProgressType = { ...emptyReport, type: 'indexing' };
    const state = reducer(undefined, setProgress(payload));
    expect(state.progress?.type).toBe('indexing');
  });

  it('U42.4 setProgress dispatched twice — last value wins', () => {
    const first: ProgressType = { ...emptyReport, type: 'first' };
    const second: ProgressType = { ...emptyReport, type: 'second' };
    let state = reducer(undefined, setProgress(first));
    state = reducer(state, setProgress(second));
    expect(state.progress?.type).toBe('second');
  });
});

describe('progressReducer – setConnectionStatus', () => {
  it('U42.5 setConnectionStatus updates socketConnection', () => {
    const state = reducer(undefined, setConnectionStatus({ status: SocketConnectionStatusType.Connected }));
    expect(state.socketConnection.status).toBe(SocketConnectionStatusType.Connected);
  });
});
