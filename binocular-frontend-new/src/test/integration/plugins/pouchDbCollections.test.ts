// IW1 — PouchDB data plugin collections
//
// Verifies that each collection's getAll() method returns the correct shape and
// count when the in-memory PouchDB is pre-seeded with fixture documents.
//
// No network or file I/O — uses pouchdb-adapter-memory (already a project dep).
// PouchDB plugins (find + adapter-memory) are self-registered when database.ts
// and utils.ts are first imported.

import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import PouchDB from 'pouchdb-browser';

// Importing these modules causes PouchDB.plugin(PouchDBFind) and
// PouchDB.plugin(PouchDBAdapterMemory) to run — required before creating instances.
import Database from '../../../plugins/dataPlugins/pouchDB/src/database.ts';
import Commits from '../../../plugins/dataPlugins/pouchDB/src/collections/commits.ts';
import Files from '../../../plugins/dataPlugins/pouchDB/src/collections/files.ts';
import Issues from '../../../plugins/dataPlugins/pouchDB/src/collections/issues.ts';
import Builds from '../../../plugins/dataPlugins/pouchDB/src/collections/builds.ts';
import Accounts from '../../../plugins/dataPlugins/pouchDB/src/collections/accounts.ts';
import General from '../../../plugins/dataPlugins/pouchDB/src/general.ts';

const FROM = '2024-01-01T00:00:00.000Z';
const TO = '2024-12-31T23:59:59.000Z';

/**
 * Creates a fresh Database instance with in-memory PouchDB stores assigned
 * directly, bypassing Database.createDB() which uses Worker detection
 * (unreliable in jsdom).
 */
function makeDatabase(): Database {
  const uid = `test_${Date.now()}_${Math.random().toString(36).slice(2)}`;
  const database = new Database();
  database.documentStore = new PouchDB(`${uid}_docs`, { adapter: 'memory' });
  database.edgeStore = new PouchDB(`${uid}_edges`, { adapter: 'memory' });
  return database;
}

describe('I8 — PouchDB data plugin collections', () => {
  let database: Database;

  beforeEach(() => {
    database = makeDatabase();
  });

  afterEach(async () => {
    await database.documentStore?.destroy();
    await database.edgeStore?.destroy();
  });

  // ── I8.1 — commits.getAll() returns all seeded commit docs ──────────────

  it('I8.1 — returns all seeded commit docs', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'commits/aaa',
        sha: 'aaa',
        shortSha: 'aaa',
        messageHeader: 'fix: a',
        message: 'fix: a',
        date: '2024-03-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      {
        _id: 'commits/bbb',
        sha: 'bbb',
        shortSha: 'bbb',
        messageHeader: 'feat: b',
        message: 'feat: b',
        date: '2024-06-01T00:00:00.000Z',
        stats: { additions: 5, deletions: 2 },
        webUrl: '',
      },
      {
        _id: 'commits/ccc',
        sha: 'ccc',
        shortSha: 'ccc',
        messageHeader: 'chore: c',
        message: 'chore: c',
        date: '2024-09-01T00:00:00.000Z',
        stats: { additions: 2, deletions: 1 },
        webUrl: '',
      },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([
      { _id: 'commits-users/1', from: 'commits/aaa', to: 'users/u1' },
      { _id: 'commits-users/2', from: 'commits/bbb', to: 'users/u1' },
      { _id: 'commits-users/3', from: 'commits/ccc', to: 'users/u1' },
    ]);

    const commits = new Commits(database);
    const result = await commits.getAll(FROM, TO);

    expect(result).toHaveLength(3);
    expect(result.map((c) => c.sha)).toEqual(expect.arrayContaining(['aaa', 'bbb', 'ccc']));
    expect(result[0]).toHaveProperty('sha');
    expect(result[0]).toHaveProperty('date');
  });

  // ── I8.2 — commits.getAll(from, to) filters by date range ───────────────

  it('I8.2 — filters commits by date range', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'commits/inside',
        sha: 'inside',
        shortSha: 'in',
        messageHeader: 'in range',
        message: 'in range',
        date: '2024-06-15T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      {
        _id: 'commits/before',
        sha: 'before',
        shortSha: 'be',
        messageHeader: 'before range',
        message: 'before range',
        date: '2023-01-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      {
        _id: 'commits/after',
        sha: 'after',
        shortSha: 'af',
        messageHeader: 'after range',
        message: 'after range',
        date: '2025-01-01T00:00:00.000Z',
        stats: { additions: 1, deletions: 0 },
        webUrl: '',
      },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([
      { _id: 'commits-users/1', from: 'commits/inside', to: 'users/u1' },
      { _id: 'commits-users/2', from: 'commits/before', to: 'users/u1' },
      { _id: 'commits-users/3', from: 'commits/after', to: 'users/u1' },
    ]);

    const commits = new Commits(database);
    const result = await commits.getAll(FROM, TO);

    expect(result).toHaveLength(1);
    expect(result[0].sha).toBe('inside');
  });

  // ── I8.3 — files.getAll() returns all seeded file docs ──────────────────

  it('I8.3 — files.getAll() returns all seeded file docs', async () => {
    await database.documentStore.bulkDocs([
      { _id: 'files/src/index.ts', path: 'src/index.ts', webUrl: '', maxLength: 100 },
      { _id: 'files/src/app.ts', path: 'src/app.ts', webUrl: '', maxLength: 200 },
    ]);

    const files = new Files(database);
    const result = await files.getAll();

    expect(result).toHaveLength(2);
    expect(result.map((f) => f.path)).toEqual(expect.arrayContaining(['src/index.ts', 'src/app.ts']));
  });

  // ── I8.4 — issues.getAll() returns all seeded issue docs ────────────────

  it('I8.4 — issues.getAll() returns all seeded issue docs', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'issues/1',
        iid: 1,
        title: 'Bug report',
        description: '',
        state: 'open',
        webUrl: '',
        createdAt: '2024-03-01T00:00:00.000Z',
        closedAt: null,
        labels: [],
      },
      {
        _id: 'issues/2',
        iid: 2,
        title: 'Feature request',
        description: '',
        state: 'closed',
        webUrl: '',
        createdAt: '2024-06-01T00:00:00.000Z',
        closedAt: '2024-07-01T00:00:00.000Z',
        labels: [],
      },
    ]);

    const issues = new Issues(database);
    const result = await issues.getAll(FROM, TO);

    expect(result).toHaveLength(2);
    expect(result.map((i) => i.iid)).toEqual(expect.arrayContaining([1, 2]));
  });

  // ── I8.5 — builds.getAll() returns all seeded build docs ────────────────

  it('I8.5 — builds.getAll() returns all seeded build docs', async () => {
    await database.documentStore.bulkDocs([
      {
        _id: 'builds/1',
        id: 1,
        status: 'success',
        createdAt: '2024-05-01T00:00:00.000Z',
        committedAt: '',
        duration: '30',
        finishedAt: '2024-05-01T00:01:00.000Z',
        jobs: [],
        startedAt: '2024-05-01T00:00:30.000Z',
        updatedAt: '2024-05-01T00:01:00.000Z',
        userFullName: '',
      },
    ]);

    const builds = new Builds(database);
    const result = await builds.getAll(FROM, TO);

    expect(result).toHaveLength(1);
    expect(result[0].status).toBe('success');
  });

  // ── I8.6 — accounts.getAll() returns all seeded account docs ────────────

  it('I8.6 — accounts.getAll() returns all seeded account docs', async () => {
    await database.documentStore.bulkDocs([
      { _id: 'accounts/1', name: 'Alice', platform: 'github', login: 'alice' },
      { _id: 'accounts/2', name: 'Bob', platform: 'github', login: 'bob' },
      { _id: 'users/u1', gitSignature: 'Alice <alice@example.com>' },
    ]);
    await database.edgeStore.bulkDocs([{ _id: 'accounts-users/1', from: 'accounts/1', to: 'users/u1' }]);

    const accounts = new Accounts(database);
    const result = await accounts.getAll();

    expect(result).toHaveLength(2);
    expect(result.map((a) => a.name)).toEqual(expect.arrayContaining(['Alice', 'Bob']));
  });

  // ── I8.7 — empty DB returns empty arrays for all collections ────────────

  it('I8.7 — empty DB returns empty arrays for all collections', async () => {
    const [c, f, i, b, a] = await Promise.all([
      new Commits(database).getAll(FROM, TO),
      new Files(database).getAll(),
      new Issues(database).getAll(FROM, TO),
      new Builds(database).getAll(FROM, TO),
      new Accounts(database).getAll(),
    ]);

    expect(c).toHaveLength(0);
    expect(f).toHaveLength(0);
    expect(i).toHaveLength(0);
    expect(b).toHaveLength(0);
    expect(a).toHaveLength(0);
  });

  // ── I8.8 — general.getIndexer() returns PouchDB indexer identifiers ─────

  it('I8.8 — general.getIndexer() returns PouchDB indexer identifiers', () => {
    const general = new General();
    const indexer = general.getIndexer();

    expect(indexer.vcs).toBe('PouchDB');
    expect(indexer.its).toBe('PouchDB');
    expect(indexer.ci).toBe('PouchDB');
  });
});
