'use strict';

import Connection from '../Connection.ts';
import Commit, { CommitDataType } from '../models/Commit';

export interface CommitCommitConnectionDataType {
  diff: {
    files: Array<{
      oldFilePath: string;
      newFilePath: string;
      status: string;
      hunks: Array<{
        oldStart: number;
        oldLines: number;
        newStart: number;
        newLines: number;
        lines: Array<{
          content: string;
          origin: string;
        }>;
      }>;
    }>;
  };
}

class CommitCommitConnection extends Connection<CommitCommitConnectionDataType, CommitDataType, CommitDataType> {
  constructor() {
    super();
  }

  ensureCollection() {
    return super.ensureCollection(Commit, Commit);
  }
}
export default new CommitCommitConnection();
