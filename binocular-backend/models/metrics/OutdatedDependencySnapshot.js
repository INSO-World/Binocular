'use strict';

import _ from 'lodash';
import { createHash } from 'crypto';
import Model from '../LegacyModel.js';
import IllegalArgumentError from '../../errors/IllegalArgumentError.js';

const OutdatedDependencySnapshot = Model.define('OutdatedDependencySnapshot', {
  attributes: [
    'id',
    'branch',
    'commitHash',
    'sequence',
    'date',
    'outdatedPercentage',
    'outdatedCount',
    'evaluatedCount',
    'totalCount',
    'unknownCount',
    'createdAt',
  ],
  keyAttribute: 'id',
});

OutdatedDependencySnapshot.keyFromData = (data) => {
  const branch = createHash('sha1')
    .update(String(data.branch || 'unknown'))
    .digest('hex')
    .slice(0, 16);
  const sha = String(data.commitHash || 'unknown').replace(/[^A-Za-z0-9_]+/g, '_');
  return `${branch}_${sha}`;
};

OutdatedDependencySnapshot.persist = function (_doc) {
  const doc = _.clone(_doc);
  if (!doc.branch || !doc.commitHash) {
    throw new IllegalArgumentError('OutdatedDependencySnapshot requires branch and commitHash!');
  }

  doc.branch = String(doc.branch);
  doc.commitHash = String(doc.commitHash);
  doc.sequence = Number.isFinite(Number(doc.sequence)) ? Number(doc.sequence) : null;
  doc.date = doc.date ? String(doc.date) : new Date().toISOString();
  doc.createdAt = doc.createdAt ? String(doc.createdAt) : new Date().toISOString();
  doc.outdatedCount = Math.max(0, Number(doc.outdatedCount) || 0);
  doc.evaluatedCount = Math.max(0, Number(doc.evaluatedCount) || 0);
  doc.totalCount = Math.max(0, Number(doc.totalCount) || 0);
  doc.unknownCount = Math.max(0, Number(doc.unknownCount) || 0);
  doc.outdatedPercentage = Number.isFinite(Number(doc.outdatedPercentage))
    ? Math.min(100, Math.max(0, Number(doc.outdatedPercentage)))
    : null;
  doc.id = OutdatedDependencySnapshot.keyFromData(doc);

  return OutdatedDependencySnapshot.ensureById(doc.id, doc, {
    ignoreUnknownAttributes: true,
  });
};

OutdatedDependencySnapshot.removeByBranch = async function (branch) {
  const normalizedBranch = String(branch || '').trim();
  if (!normalizedBranch) throw new IllegalArgumentError('removeByBranch requires branch');

  await OutdatedDependencySnapshot.rawDb
    .query({
      query: `
        FOR doc IN @@coll
          FILTER doc.branch == @branch
          REMOVE doc IN @@coll
      `,
      bindVars: {
        '@coll': OutdatedDependencySnapshot.collectionName,
        // eslint-disable-next-line quote-props
        branch: normalizedBranch,
      },
    })
    .catch((error) => console.log('[OutdatedDependencySnapshot] removeByBranch error:', error.message));
};

export default OutdatedDependencySnapshot;
