'use strict';

import Connection from '../Connection.ts';
import MergeRequest, { MergeRequestDataType } from '../models/MergeRequest.ts';
import Issue, { IssueDataType } from '../models/Issue.ts';

interface MergeRequestIssueConnectionDataType {}

class MergeRequestIssueConnection extends Connection<MergeRequestIssueConnectionDataType, MergeRequestDataType, IssueDataType> {
  constructor() {
    super();
  }

  ensureCollection() {
    return super.ensureCollection(MergeRequest, Issue);
  }
}
export default new MergeRequestIssueConnection();
