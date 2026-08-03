'use strict';

import _ from 'lodash';
import Model from './Model.js';
import IllegalArgumentError from '../errors/IllegalArgumentError.js';

const DEP_TYPES = new Set(['DIRECT', 'TRANSITIVE', 'ABSENT']);

const VersionChangeEvent = Model.define('VersionChangeEvent', {
  attributes: [
    'id',
    'commitHash',
    'branchName',
    'author',
    'timestamp',
    'sequence',
    'library',
    'oldVersion',
    'newVersion',

    // enum-like fields (validated in persist)
    'dependencyType', // 'DIRECT' | 'TRANSITIVE' | 'ABSENT'
    'wasDependencyType', // 'DIRECT' | 'TRANSITIVE' | 'ABSENT'

    'sourceType', // 'commit' | 'merge' | 'cherry-pick' | 'rebase'
    'sourceMergeCommit',
    'introducedCommits',
    'vulnerabilities',
  ],
  keyAttribute: 'id',
});

VersionChangeEvent.keyFromData = (data) => {
  const safeLibrary = String(data.library || '').replace(/[^A-Za-z0-9_]+/g, '_');
  return `${data.commitHash}_${safeLibrary}`;
};

function assertDepType(fieldName, value) {
  if (value === null || value === undefined) return;
  const v = String(value).toUpperCase();
  if (!DEP_TYPES.has(v)) {
    throw new IllegalArgumentError(`${fieldName} must be one of ${[...DEP_TYPES].join(', ')} (got "${value}")`);
  }
}

VersionChangeEvent.persist = function (_eventData) {
  const eventData = _.clone(_eventData);

  if (!eventData.commitHash || !eventData.library) {
    throw new IllegalArgumentError('VersionChangeEvent requires commitHash and library!');
  }

  eventData.commitHash = eventData.commitHash.toString();
  eventData.library = eventData.library.toString();

  // normalize + validate enum-like fields
  if (eventData.dependencyType !== null && eventData.dependencyType !== undefined) {
    eventData.dependencyType = String(eventData.dependencyType).toUpperCase();
  }
  if (eventData.wasDependencyType !== null && eventData.wasDependencyType !== undefined) {
    eventData.wasDependencyType = String(eventData.wasDependencyType).toUpperCase();
  }

  assertDepType('dependencyType', eventData.dependencyType);
  assertDepType('wasDependencyType', eventData.wasDependencyType);

  const key = VersionChangeEvent.keyFromData(eventData);

  return VersionChangeEvent.ensureById(key, eventData, {
    ignoreUnknownAttributes: true,
  });
};

VersionChangeEvent.DEPENDENCY_TYPES = Object.freeze({
  DIRECT: 'DIRECT',
  TRANSITIVE: 'TRANSITIVE',
  ABSENT: 'ABSENT',
});

export default VersionChangeEvent;
