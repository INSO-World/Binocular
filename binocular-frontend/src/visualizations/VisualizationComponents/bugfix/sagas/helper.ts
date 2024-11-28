'use strict';

import Database from '../../../../database/database';

// Most of the code copied from code-ownership, since this component uses similar dashboard layout

export async function getOwnershipForCommits(history) {
  const ownershipData = await Database.getOwnershipDataForCommits();
  return ownershipData.filter((d) => history.includes(d.sha));
}

export async function getBranches() {
  return Database.getAllBranches().then((resp) => resp.branches.data);
}

export async function getFilenamesForBranch(branchName) {
  return Database.getFilenamesForBranch(branchName);
}
