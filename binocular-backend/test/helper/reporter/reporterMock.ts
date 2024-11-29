'use strict';

import _ from 'lodash';
import * as inflection from 'inflection';

function ReporterMock(io: any, categories: string[]) {
  this.categories = {};

  _.each(categories, (categories) => {
    const Category = _.upperFirst(inflection.singularize(categories));

    this.categories[categories] = {
      processed: 0,
      total: 0,
    };

    this[`set${Category}Count`] = function (n) {
      this.categories[categories].total = n;
      this.categories[categories].processed = 0;
    };

    this[`finish${Category}`] = function () {
      this.categories[categories].processed++;
    };
  });
}

export default ReporterMock;
