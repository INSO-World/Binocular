'use strict';

import chai from 'chai';
import fs from 'fs';
import path from 'path';

const expect = chai.expect;

// Run from inside binocular-backend, the same way as every other test command (see test/base.test.ts).
const BACKEND_ROOT = path.resolve('.');
// test/, node_modules/ and build output hold no Octokit client code, so they cannot introduce an undeclared import
const SKIPPED_DIRECTORIES = ['node_modules', 'test', 'dist', 'coverage'];
const SOURCE_FILE = /\.(ts|js)$/;

// every quoted module specifier of a file, so a comment that names a package does not count as an import
const QUOTED_MODULE = /['"](@octokit\/[^'"]+)['"]/g;

/**
 * The `@octokit/...` package names a source file imports, e.g. `@octokit/core` for the specifier
 * `@octokit/core/dist-types/types`.
 */
function importedOctokitPackages(file: string): Set<string> {
  const source = fs.readFileSync(path.join(BACKEND_ROOT, file), 'utf8');
  const names = new Set<string>();
  for (const [, specifier] of source.matchAll(QUOTED_MODULE)) {
    const [scope, package_] = specifier.split('/');
    names.add(`${scope}/${package_}`);
  }
  return names;
}

function sourceFiles(): string[] {
  const found: string[] = [];
  for (const entry of fs.readdirSync(BACKEND_ROOT, { withFileTypes: true })) {
    if (entry.isDirectory() && !SKIPPED_DIRECTORIES.includes(entry.name)) {
      found.push(...sourceFilesIn(path.join(BACKEND_ROOT, entry.name)));
    } else if (!entry.isDirectory() && SOURCE_FILE.test(entry.name) && !entry.name.endsWith('.d.ts')) {
      found.push(path.relative(BACKEND_ROOT, path.join(BACKEND_ROOT, entry.name)));
    }
  }
  return found.sort();
}

function sourceFilesIn(directory: string): string[] {
  const found: string[] = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (entry.isDirectory()) {
      found.push(...sourceFilesIn(path.join(directory, entry.name)));
    } else if (SOURCE_FILE.test(entry.name) && !entry.name.endsWith('.d.ts')) {
      found.push(path.relative(BACKEND_ROOT, path.join(directory, entry.name)));
    }
  }
  return found;
}

function declaredOctokitPackages(): Record<string, string> {
  const dependencies = JSON.parse(fs.readFileSync(path.join(BACKEND_ROOT, 'package.json'), 'utf8')).dependencies;
  return Object.fromEntries(Object.entries<string>(dependencies).filter(([name]) => name.startsWith('@octokit/')));
}

function lockPackages(): Record<string, { version: string }> {
  return JSON.parse(fs.readFileSync(path.join(BACKEND_ROOT, 'package-lock.json'), 'utf8')).packages;
}

function versionNumbers(version: string): number[] {
  return version.split('-')[0].split('.').map(Number);
}

function isAtLeast(version: number[], minimum: number[]): boolean {
  for (let index = 0; index < 3; index++) {
    if (version[index] !== minimum[index]) {
      return version[index] > minimum[index];
    }
  }
  return true;
}

function satisfiesCaret(range: string, version: string): boolean {
  // shortcut: the @octokit block declares caret ranges only; teach this function another form when it declares more
  const wanted = /^\^(\d+)\.(\d+)\.(\d+)$/.exec(range);
  if (wanted === null) {
    return false;
  }
  const floor = versionNumbers(wanted.slice(1).join('.'));
  const resolved = versionNumbers(version);
  // a caret range allows the same major at or above the declared version
  return resolved[0] === floor[0] && isAtLeast(resolved, floor);
}

describe('octokit dependencies', function () {
  const files = sourceFiles();
  const imported = new Set<string>();
  for (const file of files) {
    for (const name of importedOctokitPackages(file)) {
      imported.add(name);
    }
  }
  const declared = declaredOctokitPackages();

  it('scans the source files that use Octokit', function () {
    expect(files).to.include('core/provider/github.ts');
    expect(files).to.include('indexers/ci/TravisCIIndexer.js');
    expect(imported.size).to.be.greaterThan(0);
  });

  it('declares every imported @octokit package (AC-0003-1)', function () {
    expect(Object.keys(declared)).to.have.members([...imported]);
  });

  it('imports every declared @octokit package (AC-0003-2)', function () {
    for (const name of Object.keys(declared)) {
      expect(imported.has(name), `${name} is declared but no source file imports it`).to.be.true;
    }
  });

  it('resolves every declared range in the lock (AC-0003-3)', function () {
    const packages = lockPackages();
    for (const [name, range] of Object.entries(declared)) {
      const resolved = packages[`node_modules/${name}`];
      expect(resolved, `${name} is missing from package-lock.json`).to.be.an('object');
      expect(satisfiesCaret(range, resolved.version), `${name} resolves ${resolved.version}, outside ${range}`).to.be.true;
    }
  });

  it('resolves exactly one @octokit/core copy in the lock (AC-0003-3)', function () {
    const nestedCopies = Object.keys(lockPackages()).filter((key) => /(^|\/)node_modules\/@octokit\/core$/.test(key));
    expect(nestedCopies, `package-lock.json holds ${nestedCopies.join(', ')}`).to.have.lengthOf(1);
  });
});

describe('shared GitHub client', function () {
  const source = fs.readFileSync(path.join(BACKEND_ROOT, 'core', 'provider', 'github.ts'), 'utf8');

  // AC-0005-1: each option github.ts hands the client, and the installed plugin that must read it
  const PASSED_OPTIONS: Record<string, string[]> = {
    '@octokit/plugin-retry': ['retries', 'doNotRetry'],
    '@octokit/plugin-throttling': ['onRateLimit', 'onSecondaryRateLimit'],
  };

  function installedSource(packageName: string): string {
    const manifest = JSON.parse(fs.readFileSync(path.join(BACKEND_ROOT, 'node_modules', packageName, 'package.json'), 'utf8'));
    return fs.readFileSync(path.join(BACKEND_ROOT, 'node_modules', packageName, manifest.main), 'utf8');
  }

  it('needs no cast to build the client (AC-0004-1)', function () {
    expect(source).to.not.contain('as any');
  });

  it('imports no package build directory (AC-0004-1)', function () {
    expect(source).to.not.contain('dist-types');
  });

  it('passes only options the installed plugins read (AC-0005-1)', function () {
    for (const [packageName, options] of Object.entries(PASSED_OPTIONS)) {
      const pluginSource = installedSource(packageName);
      for (const option of options) {
        expect(source, `github.ts does not pass ${option}`).to.contain(`${option}:`);
        expect(pluginSource, `${packageName} does not read ${option}`).to.contain(option);
      }
    }
  });

  it('passes no option the installed plugins dropped (AC-0005-1)', function () {
    // shouldRetryMethod only looked live because plugin-retry types its options as any, so tsc accepts dead names
    expect(source).to.not.contain('shouldRetryMethod');
  });
});
