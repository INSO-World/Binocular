// U55 — dataPluginStorage
//
// DataPluginStorage manages a runtime cache of initialised DataPlugin instances,
// keyed by plugin name+id.  It delegates construction to the `dataPlugins`
// array exported by pluginRegistry, which is mocked here.

import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { DataPlugin } from '../../../plugins/interfaces/dataPlugin.ts';

// ── mock pluginRegistry before importing the module under test ─────────────────
// vi.mock is hoisted, so FakePlugin and mockInit must be defined via vi.hoisted().

const { FakePlugin, mockInit } = vi.hoisted(() => {
  const mockInit = vi.fn().mockResolvedValue(undefined);
  class FakePlugin {
    name = 'FakeName';
    init = mockInit;
  }
  return { FakePlugin, mockInit };
});

vi.mock('../../../plugins/pluginRegistry.ts', () => ({
  dataPlugins: [FakePlugin],
}));

import DataPluginStorage from '../../../utils/dataPluginStorage.ts';

// Accessor to the private static cache without `as any`
type StorageInternals = { configuredDataPlugins: Record<string, DataPlugin> };
const internals = () => DataPluginStorage as unknown as StorageInternals;

// Reset the internal cache and mock state before every test.
beforeEach(() => {
  internals().configuredDataPlugins = {};
  vi.clearAllMocks();
});

// ─── addDataPlugin ─────────────────────────────────────────────────────────────

describe('addDataPlugin', () => {
  it('U55.1 is a no-op when id is undefined', async () => {
    await DataPluginStorage.addDataPlugin({ id: undefined, name: 'FakeName', color: '#fff', parameters: {} });
    expect(mockInit).not.toHaveBeenCalled();
    expect(Object.keys(internals().configuredDataPlugins)).toHaveLength(0);
  });

  it('U55.2 calls init on the matched plugin class', async () => {
    await DataPluginStorage.addDataPlugin({ id: 1, name: 'FakeName', color: '#fff', parameters: {} });
    expect(mockInit).toHaveBeenCalledOnce();
  });

  it('U55.3 stores the instance in the cache under name+id key', async () => {
    await DataPluginStorage.addDataPlugin({ id: 7, name: 'FakeName', color: '#fff', parameters: {} });
    expect(internals().configuredDataPlugins['FakeName7']).toBeInstanceOf(FakePlugin);
  });

  it('U55.4 passes apiKey, endpoint, fileName, and progressUpdate to init', async () => {
    const params = { apiKey: 'key', endpoint: 'http://x', fileName: 'db.json', progressUpdate: undefined };
    await DataPluginStorage.addDataPlugin({ id: 1, name: 'FakeName', color: '#fff', parameters: params });
    expect(mockInit).toHaveBeenCalledWith('key', 'http://x', { name: 'db.json', file: undefined, dbObjects: undefined }, undefined);
  });

  it('U55.5 does nothing when no plugin class matches the name', async () => {
    await DataPluginStorage.addDataPlugin({ id: 1, name: 'UnknownPlugin', color: '#fff', parameters: {} });
    expect(Object.keys(internals().configuredDataPlugins)).toHaveLength(0);
  });
});

// ─── getDataPlugin ─────────────────────────────────────────────────────────────

describe('getDataPlugin', () => {
  it('U55.6 returns undefined when id is undefined', async () => {
    const result = await DataPluginStorage.getDataPlugin({ id: undefined, name: 'FakeName', color: '#fff', parameters: {} });
    expect(result).toBeUndefined();
  });

  it('U55.7 creates and returns a plugin instance on cache miss', async () => {
    const result = await DataPluginStorage.getDataPlugin({ id: 1, name: 'FakeName', color: '#fff', parameters: {} });
    expect(result).toBeInstanceOf(FakePlugin);
  });

  it('U55.8 returns undefined when no plugin class matches', async () => {
    const result = await DataPluginStorage.getDataPlugin({ id: 1, name: 'NoSuchPlugin', color: '#fff', parameters: {} });
    expect(result).toBeUndefined();
  });

  it('U55.9 stores the created plugin in the cache under name+id key', async () => {
    await DataPluginStorage.getDataPlugin({ id: 2, name: 'FakeName', color: '#fff', parameters: {} });
    expect(internals().configuredDataPlugins['FakeName2']).toBeInstanceOf(FakePlugin);
  });

  it('U55.10 returns the cached instance added by addDataPlugin without re-calling init', async () => {
    const dp = { id: 1, name: 'FakeName', color: '#fff', parameters: {} };
    await DataPluginStorage.addDataPlugin(dp);
    const result = await DataPluginStorage.getDataPlugin(dp);
    expect(result).toBeInstanceOf(FakePlugin);
    // init must have been called exactly once (cache hit, not re-created)
    expect(mockInit).toHaveBeenCalledOnce();
  });
});
