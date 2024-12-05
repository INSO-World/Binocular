import Model from '../Model.ts';
import _ from 'lodash';
import JacocoReportDto from '../../types/dtos/JaCoCoReportDto.ts';

export interface JacocoDataType {
  id: string;
  xmlContent: string;
  buildId: string;
}

class JacocoReport extends Model<JacocoDataType> {
  constructor() {
    super({
      name: 'JacocoReport',
      keyAttribute: 'id',
    });
  }

  persist(_jacocoReportData: JacocoReportDto) {
    const jacocoReportData = _.clone(_jacocoReportData);
    if (_jacocoReportData.id) {
      jacocoReportData.id = _jacocoReportData.id.toString();
    }

    this.ensureByExample({ id: jacocoReportData.id }, jacocoReportData, {});
  }
}

export default new JacocoReport();
