'use strict';

import Database from '../../../../database/database';

// Most of the code copied from code-ownership, since this component uses similar dashboard layout

export async function getBranches() {
  return Database.getAllBranches().then((resp) => resp.branches.data);
}

export async function getFilenamesForBranch(branchName) {
  return Database.getFilenamesForBranch(branchName);
}
