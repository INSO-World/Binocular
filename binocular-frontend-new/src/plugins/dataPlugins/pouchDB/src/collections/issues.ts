import Database from '../database.ts';
import { findAllCommits, findAllIssues, findIssueCommitConnections } from '../utils.ts';
import type { DataPluginIssue, DataPluginIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';
import type { DataPluginCommit } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default class Issues implements DataPluginIssues {
  public database: Database | undefined;
  constructor(database: Database | undefined) {
    this.database = database;
  }

  public async getAll(from: string, to: string) {
    console.log(`Getting Issues from ${from} to ${to}`);
    const first = new Date(from).getTime();
    const last = new Date(to).getTime();
    if (this.database && this.database.documentStore) {
      const [{ docs: commits }, { docs: issuesCommits }, { docs: issues }] = await Promise.all([
        findAllCommits(this.database.documentStore, this.database.edgeStore),
        findIssueCommitConnections(this.database.edgeStore),

        findAllIssues(this.database.documentStore, this.database.edgeStore),
      ]);

      const issueWithCommits = new Map<string, DataPluginCommit[]>();

      for (const { from: issueId, to: commitId } of issuesCommits as unknown as {
        /** issue */
        from: string;
        /** commit */
        to: string;
      }[]) {
        if (!issueWithCommits.has(issueId)) {
          issueWithCommits.set(issueId, []);
        }

        issueWithCommits
          .get(issueId)
          ?.push(...((commits as unknown as { _id: string }[]).filter((c) => c._id === commitId) as unknown as DataPluginCommit[]));
      }

      return (issues as unknown as DataPluginIssue[])
        .filter((c) => new Date(c.createdAt).getTime() >= first && new Date(c.createdAt).getTime() <= last)
        .sort((a, b) => {
          return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
        })
        .map((i) => {
          return { ...i, commits: issueWithCommits.get((i as unknown as { _id: string })._id)?.map((c) => c.stats) ?? [] };
        });
    } else {
      return new Promise<DataPluginIssue[]>((resolve) => {
        const issue: DataPluginIssue[] = [];
        resolve(issue);
      });
    }
  }
}
