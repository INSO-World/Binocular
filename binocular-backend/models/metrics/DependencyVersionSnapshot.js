'use strict';

import _ from 'lodash';
import { createHash } from 'crypto';
import Model from '../Model.js';
import IllegalArgumentError from '../../errors/IllegalArgumentError.js';

const DependencyVersionSnapshot = Model.define('DependencyVersionSnapshot', {
  attributes: [
    'id',
    'branch',
    'library',
    'commitHash',
    'sequence',
    'date',
    'author',
    'oldVersion',
    'newVersion',
    'dependencyType',
    'wasDependencyType',
    'sourceType',
    'highestSeverity',
    'vulnerabilityCount',
    'vulnerabilities',
    'createdAt',
  ],
  keyAttribute: 'id',
});

DependencyVersionSnapshot.keyFromData = (data) =>
  createHash('sha1')
    .update(`${String(data.branch || '')}\0${String(data.commitHash || '')}\0${String(data.library || '')}`)
    .digest('hex');

DependencyVersionSnapshot.persist = function (_doc) {
  const doc = _.cloneDeep(_doc);
  if (!doc.branch || !doc.commitHash || !doc.library) {
    throw new IllegalArgumentError('DependencyVersionSnapshot requires branch, commitHash, and library!');
  }

  doc.branch = String(doc.branch);
  doc.commitHash = String(doc.commitHash);
  doc.library = String(doc.library);
  doc.sequence = Number.isFinite(Number(doc.sequence)) ? Number(doc.sequence) : null;
  doc.date = doc.date ? String(doc.date) : new Date().toISOString();
  doc.author = doc.author ? String(doc.author) : null;
  doc.oldVersion = doc.oldVersion === null || doc.oldVersion === undefined ? null : String(doc.oldVersion);
  doc.newVersion = doc.newVersion === null || doc.newVersion === undefined ? null : String(doc.newVersion);
  doc.dependencyType = String(doc.dependencyType || 'ABSENT').toUpperCase();
  doc.wasDependencyType = String(doc.wasDependencyType || 'ABSENT').toUpperCase();
  doc.sourceType = String(doc.sourceType || 'commit');
  doc.highestSeverity = String(doc.highestSeverity || 'NONE').toUpperCase();
  doc.vulnerabilities = Array.isArray(doc.vulnerabilities) ? doc.vulnerabilities : [];
  doc.vulnerabilityCount = doc.vulnerabilities.length;
  doc.createdAt = doc.createdAt ? String(doc.createdAt) : new Date().toISOString();
  doc.id = DependencyVersionSnapshot.keyFromData(doc);

  return DependencyVersionSnapshot.ensureById(doc.id, doc, { ignoreUnknownAttributes: true });
};

DependencyVersionSnapshot.removeByBranch = async function (branch) {
  const normalizedBranch = String(branch || '').trim();
  if (!normalizedBranch) throw new IllegalArgumentError('removeByBranch requires branch');
  await DependencyVersionSnapshot.rawDb
    .query({
      query: 'FOR doc IN @@coll FILTER doc.branch == @branch REMOVE doc IN @@coll',
      // eslint-disable-next-line quote-props
      bindVars: { '@coll': DependencyVersionSnapshot.collectionName, branch: normalizedBranch },
    })
    .catch((error) => console.log('[DependencyVersionSnapshot] removeByBranch error:', error.message));
};

export default DependencyVersionSnapshot;
