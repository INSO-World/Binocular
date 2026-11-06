import type { DataPluginAccountIssues } from '../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues';

export default {
  getAll: () => {
    return new Promise<DataPluginAccountIssues[]>((resolve) => {
      const accountIssues: DataPluginAccountIssues[] = [];
      resolve(accountIssues);
    });
  },
};
