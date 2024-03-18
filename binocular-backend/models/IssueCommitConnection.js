'use strict';

import Connection from './Connection';
import Issue from './Issue.js';
import Commit from './Commit.js';

const IssueCommitConnection = new Connection(Issue, Commit);

export default IssueCommitConnection;