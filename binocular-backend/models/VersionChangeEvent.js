'use strict';

import _ from 'lodash';
import Model from './LegacyModel.js';
import IllegalArgumentError from '../errors/IllegalArgumentError.js';
import { createHash } from 'crypto';

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
    'component',
    'lockfilePath',
    'manifestPath',
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
  return createHash('sha1')
    .update(
      `${String(data.branchName || '')}\0${String(data.commitHash || '')}\0${String(data.component || 'root')}\0${String(
        data.library || '',
      )}`,
    )
    .digest('hex');
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
  eventData.component = String(eventData.component || 'root');

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
