'use strict';

import _ from 'lodash';
import { createHash } from 'crypto';
import Model from '../Model.js';
import IllegalArgumentError from '../../errors/IllegalArgumentError.js';

const COUNT_FIELDS = ['compliantCount', 'partiallyCompliantCount', 'nonCompliantCount', 'unknownCount'];

const LicenseComplianceSnapshot = Model.define('LicenseComplianceSnapshot', {
  attributes: ['id', 'branch', 'commitHash', 'sequence', 'date', ...COUNT_FIELDS, 'totalCount', 'createdAt'],
  keyAttribute: 'id',
});

LicenseComplianceSnapshot.keyFromData = (data) => {
  const branch = createHash('sha1')
    .update(String(data.branch || 'unknown'))
    .digest('hex')
    .slice(0, 16);
  const sha = String(data.commitHash || 'unknown').replace(/[^A-Za-z0-9_]+/g, '_');
  return `${branch}_${sha}`;
};

LicenseComplianceSnapshot.persist = function (_doc) {
  const doc = _.clone(_doc);
  if (!doc.branch || !doc.commitHash) {
    throw new IllegalArgumentError('LicenseComplianceSnapshot requires branch and commitHash!');
  }

  doc.branch = String(doc.branch);
  doc.commitHash = String(doc.commitHash);
  doc.sequence = Number.isFinite(Number(doc.sequence)) ? Number(doc.sequence) : null;
  doc.date = doc.date ? String(doc.date) : new Date().toISOString();
  doc.createdAt = doc.createdAt ? String(doc.createdAt) : new Date().toISOString();
  for (const field of COUNT_FIELDS) doc[field] = Math.max(0, Number(doc[field]) || 0);
  doc.totalCount = COUNT_FIELDS.reduce((total, field) => total + doc[field], 0);
  doc.id = LicenseComplianceSnapshot.keyFromData(doc);

  return LicenseComplianceSnapshot.ensureById(doc.id, doc, { ignoreUnknownAttributes: true });
};

LicenseComplianceSnapshot.removeByBranch = async function (branch) {
  const normalizedBranch = String(branch || '').trim();
  if (!normalizedBranch) throw new IllegalArgumentError('removeByBranch requires branch');

  await LicenseComplianceSnapshot.rawDb
    .query({
      query: `
        FOR doc IN @@coll
          FILTER doc.branch == @branch
          REMOVE doc IN @@coll
      `,
      bindVars: {
        '@coll': LicenseComplianceSnapshot.collectionName,
        // eslint-disable-next-line quote-props
        branch: normalizedBranch,
      },
    })
    .catch((error) => console.log('[LicenseComplianceSnapshot] removeByBranch error:', error.message));
};

export default LicenseComplianceSnapshot;
