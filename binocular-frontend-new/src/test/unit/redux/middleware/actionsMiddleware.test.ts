import { describe, it, expect, vi } from 'vitest';
import actionsMiddleware from '../../../../redux/middleware/actions/actionsMiddleware';
import { setLastAction } from '../../../../redux/reducer/general/actionsReducer';

function buildMiddleware() {
  const dispatch = vi.fn();
  const store = { dispatch, getState: vi.fn() };
  const next = vi.fn();
  const invoke = actionsMiddleware()(store as never)(next);
  return { dispatch, next, invoke };
}

describe('actionsMiddleware', () => {
  it('U49.1 non-setLastAction: next is called once', () => {
    const { next, invoke } = buildMiddleware();
    invoke({ type: 'some/action', payload: 42 });
    expect(next).toHaveBeenCalledOnce();
  });

  it('U49.2 non-setLastAction: store.dispatch called with setLastAction', () => {
    const { dispatch, invoke } = buildMiddleware();
    invoke({ type: 'some/action', payload: 42 });
    expect(dispatch).toHaveBeenCalledOnce();
    const dispatchedAction = dispatch.mock.calls[0][0];
    expect(dispatchedAction.type).toBe(setLastAction.type);
    expect(dispatchedAction.payload).toEqual({ action: 'some/action', payload: 42 });
  });

  it('U49.3 setLastAction itself: next called, store.dispatch NOT called', () => {
    const { dispatch, next, invoke } = buildMiddleware();
    invoke({ type: setLastAction.type, payload: { action: 'x', payload: null } });
    expect(next).toHaveBeenCalledOnce();
    expect(dispatch).not.toHaveBeenCalled();
  });

  it('U49.4 payload is forwarded correctly inside setLastAction dispatch', () => {
    const { dispatch, invoke } = buildMiddleware();
    const originalPayload = { data: 'hello' };
    invoke({ type: 'test/action', payload: originalPayload });
    const dispatchedPayload = dispatch.mock.calls[0][0].payload;
    expect(dispatchedPayload.payload).toBe(originalPayload);
  });
});
