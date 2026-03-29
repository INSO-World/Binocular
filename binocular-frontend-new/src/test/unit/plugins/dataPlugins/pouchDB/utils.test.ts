import { describe, it, expect, vi } from 'vitest';

// PouchDB imports trigger plugin registration at module load time; mock the browser build
// so that only the pure exports are exercised.
vi.mock('pouchdb-browser', () => ({ default: { plugin: vi.fn() } }));
vi.mock('pouchdb-find', () => ({ default: {} }));
vi.mock('pouchdb-adapter-memory', () => ({ default: {} }));

const { binarySearchArray, binarySearch, sortByAttributeString } = await import('../../../../../plugins/dataPlugins/pouchDB/src/utils');

const arr = [
  { _id: 'aaa', val: 'apple' },
  { _id: 'bbb', val: 'banana' },
  { _id: 'bbb', val: 'berry' },
  { _id: 'ccc', val: 'cherry' },
];

describe('binarySearchArray', () => {
  it('U53.1 empty array returns []', () => {
    expect(binarySearchArray([], 'x', '_id')).toEqual([]);
  });

  it('U53.2 single match returns array with that element', () => {
    const result = binarySearchArray(arr, 'aaa', '_id');
    expect(result).toHaveLength(1);
    expect(result[0]._id).toBe('aaa');
  });

  it('U53.3 multiple matches returns all', () => {
    const result = binarySearchArray(arr, 'bbb', '_id');
    expect(result).toHaveLength(2);
  });

  it('U53.4 no match returns []', () => {
    expect(binarySearchArray(arr, 'zzz', '_id')).toEqual([]);
  });
});

describe('binarySearch', () => {
  it('U53.5 returns the matching element', () => {
    const result = binarySearch(arr, 'ccc', '_id');
    expect(result).not.toBeNull();
    expect(result!._id).toBe('ccc');
  });

  it('U53.6 returns null when not found', () => {
    expect(binarySearch(arr, 'zzz', '_id')).toBeNull();
  });
});

describe('sortByAttributeString', () => {
  const unsorted = [{ name: 'def' }, { name: 'abc' }, { name: 'ghi' }];

  it('U53.7 ascending sorts A → Z', () => {
    const result = sortByAttributeString([...unsorted], 'name', 'asc');
    expect(result.map((r) => r.name)).toEqual(['abc', 'def', 'ghi']);
  });

  it('U53.8 descending sorts Z → A', () => {
    const result = sortByAttributeString([...unsorted], 'name', 'desc');
    expect(result.map((r) => r.name)).toEqual(['ghi', 'def', 'abc']);
  });
});
