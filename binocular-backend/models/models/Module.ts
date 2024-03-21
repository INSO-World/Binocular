'use strict';

import Model from '../Model.ts';
import IllegalArgumentError from '../../errors/IllegalArgumentError.js';

import debug from 'debug';

const log = debug('git:commit:module');

export interface ModuleDao {
  path: string;
}
class Module extends Model<ModuleDao> {
  constructor() {
    super({ name: 'Module' });
  }

  /**
   * get or create a new module based on its path
   *
   * @param _moduleData
   * @returns Module returns an already existing or newly created module
   */
  async persist(_moduleData: any) {
    if (!_moduleData || !_moduleData.path) {
      throw IllegalArgumentError('Module does not hold the required data!');
    }

    const path = _moduleData.path.toString();
    //delete data.path;
    const [instance] = await this.ensureBy('path', path, _moduleData, { ignoreUnknownAttributes: true });
    log(`Finished persisted ${path} with ${instance.data.path} and ${instance._id}!`);
    return instance;
  }
}

export default new Module();
