'use strict';

import _ from 'lodash';
import Model from '../Model';
import Mention from '../../types/supportingTypes/Mention';
import MergeRequestDto from '../../types/dtos/MergeRequestDto';

export interface MergeRequestDataType {
  id: string;
  iid: number;
  title: string;
  description: string;
  createdAt: string;
  closedAt: string;
  updatedAt: string;
  labels: string[];
  state: string;
  webUrl: string;
  mentions: Mention[];
}

class MergeRequest extends Model<MergeRequestDataType> {
  constructor() {
    super({
      name: 'MergeRequest',
      keyAttribute: 'id',
    });
  }
  persist(_mergeRequestData: MergeRequestDto) {
    const mergeRequestData = _.clone(_mergeRequestData);
    if (_mergeRequestData.id) {
      mergeRequestData.id = _mergeRequestData.id.toString();
    }

    delete mergeRequestData.projectId;

    return this.ensureByExample({ id: mergeRequestData.id }, mergeRequestData, {});
  }
}

export default new MergeRequest();
