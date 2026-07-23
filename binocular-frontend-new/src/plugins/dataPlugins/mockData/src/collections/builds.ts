import type { DataPluginBuild, DataPluginBuilds } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import buildData from '../data/builds.json.zip';

const builds = buildData as unknown as DataPluginBuild[];

export default class Builds implements DataPluginBuilds {
  public async getAll(_from: string, _to: string) {
    return builds;
  }
}
