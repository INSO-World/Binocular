import type { DataPluginCommitFile, DataPluginCommitsFiles } from '../../../../interfaces/dataPluginInterfaces/dataPluginCommitsFiles.ts';
import commitData from '../data/commits.json.zip';
import fileData from '../data/files.json.zip';
import { loadCommitsFiles, groupBy } from '../lazyData.ts';

const filesById = new Map((fileData as { _id: string }[]).map((f) => [f._id, f]));
const commitBySha = new Map((commitData as { sha: string; _id: string }[]).map((c) => [c.sha, c._id]));

export default class CommitsFiles implements DataPluginCommitsFiles {
  public async getAll(sha: string): Promise<DataPluginCommitFile[]> {
    const commitId = commitBySha.get(sha);
    if (!commitId) return [];
    const cf = await loadCommitsFiles();
    const cfByCommit = groupBy(cf, (e) => e._from);
    return (cfByCommit.get(commitId) ?? []).map((e) => ({
      file: { path: filesById.get(e._to)?.path ?? '' },
      stats: e.stats,
    }));
  }
}
