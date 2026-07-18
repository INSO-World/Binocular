import type {
  CommitWithFileChanges,
  DataPluginCommit,
  DataPluginCommits,
  DataPluginCommitBuild,
  DataPluginCommitShort,
  DataPluginOwnership,
} from '../../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';
import commitData from '../data/commits.json.zip';
import fileData from '../data/files.json.zip';
import userData from '../data/users.json.zip';
import { loadCommitsFiles, loadCommitsFilesUsers, groupBy } from '../lazyData.ts';

const commits = commitData as unknown as DataPluginCommit[];
const commitsWithBuilds = commitData as unknown as DataPluginCommitBuild[];

const filesById = new Map((fileData as { _id: string; path: string; webUrl: string; maxLength: number }[]).map((f) => [f._id, f]));
const usersById = new Map((userData as { _id: string; gitSignature: string }[]).map((u) => [u._id, u]));

export default class Commits implements DataPluginCommits {
  public async getAll(from: string, to: string) {
    console.log(`Getting Commits from ${from} to ${to}`);
    return this.getCommitsWithFiles(from, to);
  }

  public async getAllShort(): Promise<DataPluginCommitShort[]> {
    return commits.map((c) => ({ sha: c.sha, date: c.date, messageHeader: c.messageHeader }));
  }

  public async getCommitsWithBuilds(_from: string, _to: string) {
    return commitsWithBuilds;
  }

  public async getCommitsWithFiles(_from: string, _to: string): Promise<DataPluginCommit[]> {
    const cf = await loadCommitsFiles();
    const cfByCommit = groupBy(cf, (e) => e._from);
    return commits.map((c) => ({
      ...c,
      files: {
        data: (cfByCommit.get((c as unknown as { _id: string })._id) ?? []).map((e) => {
          const file = filesById.get(e._to);
          return {
            file: { path: file?.path ?? '', webUrl: file?.webUrl ?? '', maxLength: file?.maxLength ?? 0 },
            stats: e.stats,
            hunks: e.hunks,
          };
        }),
      },
    }));
  }

  public async getOwnershipDataForCommits(): Promise<DataPluginOwnership[]> {
    const cf = await loadCommitsFiles();
    const cfu = await loadCommitsFilesUsers();
    const cfByCommit = groupBy(cf, (e) => e._from);
    const cfuByCf = groupBy(cfu, (e) => e._from);

    return (commitData as unknown as { _id: string; sha: string; date: string; parents: string[] }[]).map((c) => ({
      sha: c.sha,
      date: c.date,
      parents: c.parents,
      files: (cfByCommit.get(c._id) ?? []).map((cfEdge) => ({
        path: filesById.get(cfEdge._to)?.path ?? '',
        action: cfEdge.action,
        ownership: (cfuByCf.get(cfEdge._id) ?? []).map((cfuEdge) => ({
          user: usersById.get(cfuEdge._to)?.gitSignature ?? '',
          hunks: cfuEdge.hunks.map((h) => ({ originalCommit: h.oc, lines: h.lines })),
        })),
      })),
    }));
  }

  public async getCommitDataForSha(sha: string): Promise<DataPluginCommit | undefined> {
    return commits.find((c) => c.sha === sha);
  }

  public async getByFile(file: string): Promise<DataPluginCommit[]> {
    const cf = await loadCommitsFiles();
    const cfByCommit = groupBy(cf, (e) => e._from);
    const commitsById = new Map(commits.map((c) => [(c as unknown as { _id: string })._id, c]));
    const result: DataPluginCommit[] = [];
    for (const [commitId, edges] of cfByCommit) {
      const fileEdge = edges.find((e) => filesById.get(e._to)?.path === file);
      if (!fileEdge) continue;
      const commit = commitsById.get(commitId);
      if (!commit) continue;
      const fileInfo = filesById.get(fileEdge._to);
      result.push({
        ...commit,
        files: {
          data: [
            {
              file: { path: fileInfo?.path ?? '', webUrl: fileInfo?.webUrl ?? '', maxLength: fileInfo?.maxLength ?? 0 },
              stats: fileEdge.stats,
              hunks: fileEdge.hunks,
            },
          ],
        },
      });
    }
    return result;
  }

  public async getDateOfFirstCommit() {
    return [...commits].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())[0]?.date ?? '';
  }

  public async getDateOfLastCommit() {
    return [...commits].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())[0]?.date ?? '';
  }

  /**
   * Returns commits with their changed files (per-file stats and a `signature` field)
   * in the shape expected by the change-frequency visualization.
   */
  public async getCommitDataWithFilesAndOwnership(from: string, to: string): Promise<CommitWithFileChanges[]> {
    // from/to is the significant (visible) window. The mock data has no per-commit line count, so only
    // that window is kept and every returned commit is significant.
    const fromTime = new Date(from).getTime();
    const toTime = new Date(to).getTime();
    const commits = await this.getCommitsWithFiles(from, to);

    return commits
      .filter((commit) => {
        const time = new Date(commit.date).getTime();
        return time >= fromTime && time <= toTime;
      })
      .map((commit) => ({
        sha: commit.sha,
        date: commit.date,
        signature: commit.user?.gitSignature,
        branch: commit.branch,
        message: commit.message,
        webUrl: commit.webUrl,
        parents: commit.parents,
        stats: commit.stats,
        files: {
          data: (commit.files?.data ?? []).map((file) => ({
            file: { path: file.file.path },
            stats: file.stats ?? { additions: 0, deletions: 0 },
          })),
        },
        isSignificant: true,
      }));
  }
}
