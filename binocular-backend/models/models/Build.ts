'use strict';

import _ from 'lodash';
import Model, { Entry } from '../Model.ts';
import { aql } from 'arangojs';
import Job from '../../types/supportingTypes/Job';
import BuildDto from '../../types/dtos/BuildDto';
import ctx from '../../utils/context';

export interface BuildDataType {
  id: number;
  user: string;
  userFullName: string;
  committedAt: string;
  createdAt: string;
  startedAt: string;
  updatedAt: string;
  webUrl: string;
  tag: string;
  status: string;
  duration: number;
  sha: string;
  jobs: Job[];
  // true when the jobs array comes from the CI API; false/absent means the build was stored without job details
  jobsFetched?: boolean;
}

class Build extends Model<BuildDataType> {
  constructor() {
    super({
      name: 'Build',
      keyAttribute: 'id',
    });
  }
  persist(_buildData: BuildDto) {
    const buildData = _.clone(_buildData);
    if (_buildData.id) {
      buildData.id = _buildData.id;
    }
    // if jobs should be loaded, existing builds must be refreshed with the fetched data:
    // ensureByExample would silently discard the refetched build, so the refetch condition in CIIndexer would never clear
    if (ctx.argv.jobs || ctx.argv.updateJobs) {
      return this.ensureByExampleForceUpdate({ id: buildData.id }, buildData);
    }

    return this.ensureByExample({ id: buildData.id }, buildData, {});
  }

  /**
   * Ensures that an object exists in the database. If it does not exist, it will be created, else will be updated.
   * Special version of ensureByExample in Model.ts
   *
   * @param example object to search for if in database
   * @param data object to create or update depending on if exists
   */
  async ensureByExampleForceUpdate(example: object, data: BuildDataType) {
    const d = Object.assign({}, data, example);
    return this.findOneByExample(example).then(
      function (resp: Entry<BuildDataType> | null) {
        if (resp) {
          // isNew must be false, otherwise save() inserts, hits the duplicate _key, and its error handler
          // falls back to ensure(), which returns the old document without ever writing the new data
          const entry = new Entry<BuildDataType>(d, { isNew: false });
          entry._id = resp._id;
          entry._key = resp._key;
          return this.save(entry).then((i) => [i, i.justUpdated === true]);
        } else {
          const entry = new Entry<BuildDataType>(d, {
            isNew: true,
          });
          return this.save(entry).then((i) => [i, i.justCreated === true]);
        }
      }.bind(this),
    );
  }

  deleteShaRefAttributes() {
    if (this.rawDb === undefined) {
      throw Error('Database undefined!');
    }
    return this.rawDb.query(
      aql`
    FOR b IN builds
    REPLACE b WITH UNSET(b, "sha", "ref") IN builds`,
    );
  }
}

export default new Build();
