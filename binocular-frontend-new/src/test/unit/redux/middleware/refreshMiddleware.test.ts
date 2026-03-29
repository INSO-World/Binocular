import { describe, it, expect, vi } from 'vitest';
import refreshMiddleware from '../../../../redux/middleware/refresh/refreshMiddleware';
import type { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType';

const testPlugin: DatabaseSettingsDataPluginType = { id: 7, name: 'Test', color: '#fff', isDefault: true, parameters: {} };

function buildMiddleware() {
  const globalDispatch = vi.fn();
  const globalStore = { dispatch: globalDispatch, getState: vi.fn() };
  const next = vi.fn();
  const invoke = refreshMiddleware(globalStore as never, testPlugin)(undefined as never)(next);
  return { globalDispatch, next, invoke };
}

describe('refreshMiddleware', () => {
  it('U51.1 setProgress action: next is called', () => {
    const { next, invoke } = buildMiddleware();
    invoke({ type: 'progress/setProgress', payload: 50 });
    expect(next).toHaveBeenCalledOnce();
  });

  it('U51.2 setProgress action: globalStore.dispatch called with REFRESH_PLUGIN', () => {
    const { globalDispatch, invoke } = buildMiddleware();
    invoke({ type: 'progress/setProgress', payload: 50 });
    expect(globalDispatch).toHaveBeenCalledOnce();
    expect(globalDispatch).toHaveBeenCalledWith({ type: 'REFRESH_PLUGIN', payload: { pluginId: testPlugin.id } });
  });

  it('U51.3 unrelated action: next called, globalStore.dispatch NOT called', () => {
    const { globalDispatch, next, invoke } = buildMiddleware();
    invoke({ type: 'some/other', payload: 0 });
    expect(next).toHaveBeenCalledOnce();
    expect(globalDispatch).not.toHaveBeenCalled();
  });
});
