'use strict';

export const LICENSE_CATEGORIES = Object.freeze(['COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT', 'UNKNOWN']);

function licenseText(value) {
  if (typeof value === 'string') return value.trim();
  if (Array.isArray(value)) return value.map(licenseText).filter(Boolean).join(' OR ');
  if (value && typeof value === 'object') return licenseText(value.type || value.name);
  return '';
}

function classifySingleLicense(value) {
  const license = licenseText(value).toUpperCase();
  if (!license) return 'UNKNOWN';
  if (/UNLICENSED|PROPRIETARY|SEE LICENSE|BUSL|BSL-[2-9]|SSPL|AGPL|(?:^|[^L])GPL/.test(license)) return 'NON_COMPLIANT';
  if (/LGPL|MPL|EPL|CDDL|CPL|OSL|EUPL/.test(license)) return 'PARTIALLY_COMPLIANT';
  if (/MIT|ISC|BSD|APACHE|CC0|UNLICENSE|ZLIB|ARTISTIC|WTFPL|0BSD|BSL-1\.0|BLUEOAK|PYTHON/.test(license)) {
    return 'COMPLIANT';
  }
  return 'UNKNOWN';
}

const CATEGORY_RANK = {
  COMPLIANT: 0,
  UNKNOWN: 1,
  PARTIALLY_COMPLIANT: 2,
  NON_COMPLIANT: 3,
};

export function classifyLicense(value) {
  const expression = licenseText(value);
  if (!expression) return 'UNKNOWN';

  const alternatives = expression.split(/\s+OR\s+/i);
  const alternativeCategories = alternatives.map((alternative) => {
    const requirements = alternative.split(/\s+AND\s+/i).map(classifySingleLicense);
    return requirements.reduce((worst, category) => (CATEGORY_RANK[category] > CATEGORY_RANK[worst] ? category : worst), 'COMPLIANT');
  });

  return alternativeCategories.reduce(
    (best, category) => (CATEGORY_RANK[category] < CATEGORY_RANK[best] ? category : best),
    'NON_COMPLIANT',
  );
}

function installedLicense(name, dependency, packageLicensesByName) {
  if (dependency?.license) return dependency.license;
  const versions = packageLicensesByName instanceof Map ? packageLicensesByName.get(name) : null;
  if (!(versions instanceof Map)) return null;
  return versions.get(String(dependency?.version || '').trim()) ?? null;
}

export function calculateLicenseComplianceTimeline({ branchHistories, packageLicensesByName, createdAt } = {}) {
  const histories = Array.isArray(branchHistories) ? branchHistories : [];
  const generatedAt = createdAt || new Date().toISOString();
  const evaluationByCommit = new Map();
  const snapshots = [];

  for (const history of histories) {
    const branch = String(history?.branch || '').trim();
    if (!branch) continue;

    for (const commit of Array.isArray(history?.commits) ? history.commits : []) {
      const commitHash = String(commit?.commitHash || '').trim();
      const timestamp = Number(commit?.timestamp);
      if (!commitHash || !Number.isFinite(timestamp)) continue;

      let evaluation = evaluationByCommit.get(commitHash);
      if (!evaluation) {
        const counts = Object.fromEntries(LICENSE_CATEGORIES.map((category) => [category, 0]));
        const dependencies = Object.entries(commit?.dependencies || {});
        for (const [name, dependency] of dependencies) {
          counts[classifyLicense(installedLicense(name, dependency, packageLicensesByName))]++;
        }
        evaluation = {
          compliantCount: counts.COMPLIANT,
          partiallyCompliantCount: counts.PARTIALLY_COMPLIANT,
          nonCompliantCount: counts.NON_COMPLIANT,
          unknownCount: counts.UNKNOWN,
          totalCount: dependencies.length,
        };
        evaluationByCommit.set(commitHash, evaluation);
      }

      snapshots.push({
        branch,
        commitHash,
        sequence: Number.isFinite(Number(commit?.sequence)) ? Number(commit.sequence) : null,
        date: new Date(timestamp * 1000).toISOString(),
        ...evaluation,
        createdAt: generatedAt,
      });
    }
  }

  return {
    snapshots,
    diagnostics: {
      branchesProcessed: histories.length,
      commitsProcessed: snapshots.length,
      uniqueCommitsEvaluated: evaluationByCommit.size,
    },
  };
}
