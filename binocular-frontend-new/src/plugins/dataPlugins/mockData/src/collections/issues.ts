import type { DataPluginIssue, DataPluginIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';
import issueData from '../data/issues.json.zip';

const issues = issueData as unknown as DataPluginIssue[];

export default class Issues implements DataPluginIssues {
  public async getAll(_from: string, _to: string) {
    return issues;
  }
}
