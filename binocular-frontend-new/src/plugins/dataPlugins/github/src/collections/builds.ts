import type { DataPluginBuild, DataPluginBuilds } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export default class Builds implements DataPluginBuilds {
  constructor() {}
  public async getAll() {
    return await Promise.resolve(this.getBuilds());
  }
  private async getBuilds(): Promise<DataPluginBuild[]> {
    // TODO: find out if implementation is needed in any near future?
    await new Promise((resolve) => setTimeout(resolve, 10));
    return [];
  }
}
