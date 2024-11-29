import { DataPluginIssue } from '../../../interfaces/dataPluginInterfaces/dataPluginIssues.ts';

// not implemented
export default {
  getAll: () => {
    console.log(`Getting Issues`);
    return new Promise<DataPluginIssue[]>((resolve) => {
      const issues: DataPluginIssue[] = [];
      resolve(issues);
    });
  },
};
