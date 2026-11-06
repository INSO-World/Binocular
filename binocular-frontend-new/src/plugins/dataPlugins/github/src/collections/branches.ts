import { DataPluginBranch } from '../../../../interfaces/dataPluginInterfaces/dataPluginBranches';

export default {
  getAll: () => {
    return new Promise<DataPluginBranch[]>((resolve) => {
      const branches: DataPluginBranch[] = [];
      resolve(branches);
    });
  },
};
