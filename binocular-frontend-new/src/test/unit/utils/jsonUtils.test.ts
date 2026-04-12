// U56 — json-utils (compressJson / decompressJson)
//
// json-utils.ts exposes two public functions:
//   compressJson(collectionName, input[])  — strips ArangoDB meta-fields, removes redundant prefixes
//   decompressJson(collectionName, input[]) — inverse of compressJson
//
// The private helpers (compressCollectionJson, compressConnectionJson,
// compressOwnershipConnection and their decompress counterparts) are exercised
// through the public API.

import { describe, it, expect } from 'vitest';
import { compressJson, decompressJson } from '../../../utils/json-utils.ts';
import type { JSONObject } from '../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

// ─── helpers ──────────────────────────────────────────────────────────────────

/** Deep-clone to avoid mutation side effects between test cases. */
function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v));
}

// ─── compressJson – plain collection (no dash in name) ────────────────────────

describe('compressJson – plain collection', () => {
  const INPUT = [{ _id: 'commits/abc123', _key: 'abc123', _rev: '_xyz', sha: 'abc' }];

  it('U55.1 strips _key from every item', () => {
    const result = compressJson('commits', clone(INPUT)) as JSONObject[];
    expect(result[0]).not.toHaveProperty('_key');
  });

  it('U55.2 strips _rev from every item', () => {
    const result = compressJson('commits', clone(INPUT)) as JSONObject[];
    expect(result[0]).not.toHaveProperty('_rev');
  });

  it('U55.3 removes the collection prefix from _id', () => {
    const result = compressJson('commits', clone(INPUT)) as JSONObject[];
    expect(result[0]._id).toBe('abc123');
  });

  it('U55.4 preserves other data fields', () => {
    const result = compressJson('commits', clone(INPUT)) as JSONObject[];
    expect(result[0].sha).toBe('abc');
  });

  it('U55.5 handles empty input', () => {
    expect(compressJson('commits', [])).toEqual([]);
  });
});

// ─── decompressJson – plain collection ────────────────────────────────────────

describe('decompressJson – plain collection', () => {
  it('U55.6 restores the collection prefix to _id', () => {
    const result = decompressJson('commits', [{ _id: 'abc123' }]);
    expect(result[0]._id).toBe('commits/abc123');
  });

  it('U55.7 skips decompression when _id already contains "/" (already decompressed)', () => {
    const input = [{ _id: 'commits/abc123' }];
    const result = decompressJson('commits', clone(input));
    // Input is returned as-is because _id contains '/'
    expect(result[0]._id).toBe('commits/abc123');
  });

  it('U55.8 handles empty input', () => {
    expect(decompressJson('commits', [])).toEqual([]);
  });
});

// ─── compressJson – simple connection (one dash, e.g. "commits-files") ────────

describe('compressJson – simple connection', () => {
  const INPUT = [{ _id: 'commits-files/x', _key: 'x', _rev: '_r', _from: 'commits/a1', _to: 'files/b2' }];

  it('U55.9 strips prefix from _from', () => {
    const result = compressJson('commits-files', clone(INPUT)) as JSONObject[];
    expect(result[0]._from).toBe('a1');
  });

  it('U55.10 strips prefix from _to', () => {
    const result = compressJson('commits-files', clone(INPUT)) as JSONObject[];
    expect(result[0]._to).toBe('b2');
  });
});

// ─── decompressJson – simple connection ───────────────────────────────────────

describe('decompressJson – simple connection', () => {
  it('U55.11 restores prefix to _from using first segment of collection name', () => {
    const result = decompressJson('commits-files', [{ _id: 'x', _from: 'a1', _to: 'b2' }]);
    expect(result[0]._from).toBe('commits/a1');
  });

  it('U55.12 restores prefix to _to using second segment of collection name', () => {
    const result = decompressJson('commits-files', [{ _id: 'x', _from: 'a1', _to: 'b2' }]);
    expect(result[0]._to).toBe('files/b2');
  });
});

// ─── compressJson – registered 3-part connection (commits-files-users) ────────

describe('compressJson – registered complex connection (commits-files-users)', () => {
  const INPUT = [
    {
      _id: 'commits-files-users/y',
      _key: 'y',
      _rev: '_r',
      _from: 'commits-files/cf1',
      _to: 'users/u1',
      hunks: [],
    },
  ];

  it('U55.13 strips prefix from _from', () => {
    const result = compressJson('commits-files-users', clone(INPUT)) as JSONObject[];
    expect(result[0]._from).toBe('cf1');
  });

  it('U55.14 strips prefix from _to', () => {
    const result = compressJson('commits-files-users', clone(INPUT)) as JSONObject[];
    expect(result[0]._to).toBe('u1');
  });
});

// ─── compressJson – unregistered 3-part connection ────────────────────────────

describe('compressJson – unregistered complex connection (3+ parts, not in connections map)', () => {
  const INPUT = [{ _id: 'foo-bar-baz/z', _key: 'z', _rev: '_r', _from: 'foo-bar/x', _to: 'baz/y' }];

  it('U55.15 leaves _from unchanged because the mapping is unknown', () => {
    const result = compressJson('foo-bar-baz', clone(INPUT)) as JSONObject[];
    expect(result[0]._from).toBe('foo-bar/x');
  });

  it('U55.16 leaves _to unchanged because the mapping is unknown', () => {
    const result = compressJson('foo-bar-baz', clone(INPUT)) as JSONObject[];
    expect(result[0]._to).toBe('baz/y');
  });
});

// ─── decompressJson – registered 3-part connection ────────────────────────────

describe('decompressJson – registered complex connection (commits-files-users)', () => {
  // hunks is required: decompressOwnershipConnection always calls .map() on it
  const INPUT = [{ _id: 'y', _from: 'cf1', _to: 'u1', hunks: [] }];

  it('U55.17 restores _from using the registered "from" collection', () => {
    const result = decompressJson('commits-files-users', clone(INPUT));
    expect(result[0]._from).toBe('commits-files/cf1');
  });

  it('U55.18 restores _to using the registered "to" collection', () => {
    const result = decompressJson('commits-files-users', clone(INPUT));
    expect(result[0]._to).toBe('users/u1');
  });
});

// ─── ownership connection (commits-files-users) compress / decompress ─────────

describe('compressJson – ownership connection hunks', () => {
  const INPUT = [
    {
      _id: 'commits-files-users/h',
      _key: 'h',
      _rev: '_r',
      _from: 'commits-files/cf1',
      _to: 'users/u1',
      hunks: [
        {
          originalCommit: 'sha1',
          lines: [
            { from: 1, to: 5 },
            { from: 10, to: 15 },
          ],
        },
      ],
    },
  ];

  it('U55.19 renames originalCommit to oc', () => {
    const result = compressJson('commits-files-users', clone(INPUT)) as JSONObject[];
    const hunk = (result[0].hunks as JSONObject[])[0];
    expect(hunk.oc).toBe('sha1');
    expect(hunk.originalCommit).toBeUndefined();
  });

  it('U55.20 encodes line objects as "from,to" strings', () => {
    const result = compressJson('commits-files-users', clone(INPUT)) as JSONObject[];
    const hunk = (result[0].hunks as JSONObject[])[0];
    expect(hunk.lines).toEqual(['1,5', '10,15']);
  });
});

describe('decompressJson – ownership connection hunks', () => {
  const INPUT = [
    {
      _id: 'h',
      _from: 'cf1',
      _to: 'u1',
      hunks: [{ oc: 'sha1', lines: ['1,5', '10,15'] }],
    },
  ];

  it('U55.21 renames oc back to originalCommit', () => {
    const result = decompressJson('commits-files-users', clone(INPUT));
    const hunk = (result[0].hunks as JSONObject[])[0];
    expect(hunk.originalCommit).toBe('sha1');
    expect(hunk.oc).toBeUndefined();
  });

  it('U55.22 decodes line strings back into { from, to } objects', () => {
    const result = decompressJson('commits-files-users', clone(INPUT));
    const hunk = (result[0].hunks as JSONObject[])[0];
    expect(hunk.lines).toEqual([
      { from: '1', to: '5' },
      { from: '10', to: '15' },
    ]);
  });
});

// ─── roundtrip: compress → decompress ─────────────────────────────────────────

describe('roundtrip – plain collection', () => {
  it('U55.23 restores _id prefix but not _key / _rev (they are intentionally dropped)', () => {
    const original = [{ _id: 'commits/abc', _key: 'abc', _rev: '_r', sha: 'abc123' }];
    const restored = decompressJson('commits', compressJson('commits', clone(original)));
    expect(restored[0]._id).toBe('commits/abc');
    expect(restored[0]).not.toHaveProperty('_key');
    expect(restored[0]).not.toHaveProperty('_rev');
    expect(restored[0].sha).toBe('abc123');
  });
});

describe('roundtrip – simple connection', () => {
  it('U55.24 restores _from and _to with correct prefixes', () => {
    const original = [{ _id: 'commits-files/x', _key: 'x', _rev: '_r', _from: 'commits/a', _to: 'files/b' }];
    const restored = decompressJson('commits-files', compressJson('commits-files', clone(original)));
    expect(restored[0]._from).toBe('commits/a');
    expect(restored[0]._to).toBe('files/b');
  });
});

describe('roundtrip – registered complex connection (commits-files-users)', () => {
  it('U55.25 restores _from, _to, originalCommit, and lines for the registered mapping', () => {
    const original = [
      {
        _id: 'commits-files-users/y',
        _key: 'y',
        _rev: '_r',
        _from: 'commits-files/cf1',
        _to: 'users/u1',
        hunks: [{ originalCommit: 'sha42', lines: [{ from: 3, to: 7 }] }],
      },
    ];
    const restored = decompressJson('commits-files-users', compressJson('commits-files-users', clone(original)));
    expect(restored[0]._from).toBe('commits-files/cf1');
    expect(restored[0]._to).toBe('users/u1');
    const hunk = (restored[0].hunks as JSONObject[])[0];
    expect(hunk.originalCommit).toBe('sha42');
    expect(hunk.lines).toEqual([{ from: '3', to: '7' }]);
  });
});
