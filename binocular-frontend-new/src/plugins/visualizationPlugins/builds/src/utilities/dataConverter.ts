import moment from 'moment/moment';
import { BuildChartData, Palette } from '../chart/chart.tsx';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import chroma from 'chroma-js';
import _ from 'lodash';
import { DataPluginBuild } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export function convertBuildDataToChartData(
  builds: DataPluginBuild[],
  parameters: ParametersType,
): {
  chartData: BuildChartData[];
  scale: number[];
  palette: Palette;
} {
  if (!builds || builds.length === 0) {
    return { chartData: [], palette: {}, scale: [] };
  }

  //Sort builds after their build time in case they arnt sorted
  const sortedBuilds = _.clone(builds).sort((c1, c2) => new Date(c1.createdAt).getTime() - new Date(c2.createdAt).getTime());

  const firstTimestamp = sortedBuilds[0].createdAt;
  const lastTimestamp = sortedBuilds[sortedBuilds.length - 1].createdAt;

  const data: Array<{ date: number; statsByStatus: { [signature: string]: { count: number } } }> = [];
  const chartData: BuildChartData[] = [];
  const scale: number[] = [0, 0];
  const palette: Palette = {};

  if (sortedBuilds.length > 0) {
    //---- STEP 1: AGGREGATE BUILDS GROUPED BY STATUS PER TIME INTERVAL ----
    const granularity = getGranularity(parameters.parametersGeneral.granularity);
    const curr = moment(firstTimestamp)
      .startOf(granularity.unit as moment.unitOfTime.StartOf)
      .subtract(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
    const end = moment(lastTimestamp)
      .endOf(granularity.unit as moment.unitOfTime.StartOf)
      .add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
    const next = moment(curr).add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
    const totalBuildsPerStatus: { [signature: string]: number } = {};
    const statuses = ['failed', 'success', 'cancelled'];
    for (
      let i = 0;
      curr.isSameOrBefore(end);
      curr.add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity),
        next.add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity)
    ) {
      //Iterate through time buckets
      const currTimestamp = curr.toDate().getTime();
      const nextTimestamp = next.toDate().getTime();
      const obj: { date: number; statsByStatus: { [signature: string]: { count: number } } } = {
        date: currTimestamp,
        statsByStatus: {},
      }; //Save date of time bucket, create object
      for (; i < sortedBuilds.length && Date.parse(sortedBuilds[i].createdAt) < nextTimestamp; i++) {
        //Iterate through builds that fall into this time bucket
        let buildStatus = sortedBuilds[i].status;
        if (!statuses.includes(buildStatus)) {
          buildStatus = 'others';
        }
        if (totalBuildsPerStatus[buildStatus] === null) {
          totalBuildsPerStatus[buildStatus] = 0;
        }
        totalBuildsPerStatus[buildStatus] += 1;

        if (buildStatus in obj.statsByStatus) {
          obj.statsByStatus[buildStatus].count += 1;
        } else {
          obj.statsByStatus[buildStatus] = { count: 1 };
        }
        data.push(obj);
      }
    }

    //---- STEP 2: CONSTRUCT CHART DATA FROM AGGREGATED BUILDS ----

    palette['failed'] = { main: chroma('#ff0000').hex(), secondary: chroma('#FF737399').hex() };
    palette['success'] = { main: chroma('#00FF00').hex(), secondary: chroma('#73FF7399').hex() };
    palette['cancelled'] = { main: chroma('#FFA500').hex(), secondary: chroma('#FFD58099').hex() };
    palette['others'] = { main: chroma('#555555').hex(), secondary: chroma('#77777799').hex() };

    data.forEach((build) => {
      //build has structure {date, statsByStatus: {}} (see next line)}
      const obj: BuildChartData = { date: build.date };

      for (const status of statuses) {
        obj[status] = 0;
      }
      obj['others'] = 0;

      statuses.forEach((status) => {
        if (status in build.statsByStatus) {
          obj[status] = build.statsByStatus[status].count;
        }
      });
      chartData.push(obj); //Add object to list of objects
    });
    chartData.forEach((dataPoint) => {
      if (dataPoint['failed'] > 0) dataPoint['failed'] = -dataPoint['failed'];
      if (dataPoint['cancelled'] > 0) dataPoint['cancelled'] = -dataPoint['cancelled'];
      if (dataPoint['others'] > 0) dataPoint['others'] = -dataPoint['others'];
    });

    //Output in chartData has format [{author1: 123, author2: 123, ...}, ...],
    //e.g. series names are the authors with their corresponding values

    //---- STEP 3: SCALING ----
    chartData.forEach((dataPoint) => {
      let positiveTotals = 0;
      let negativeTotals = 0;
      Object.keys(dataPoint)
        .splice(1)
        .forEach((key) => {
          if (key.includes('success')) {
            positiveTotals += dataPoint[key];
          } else {
            negativeTotals += dataPoint[key];
          }
        });
      if (positiveTotals > scale[1]) {
        scale[1] = positiveTotals;
      }
      if (negativeTotals < scale[0]) {
        scale[0] = negativeTotals;
      }
    });
  }
  return { chartData: chartData, scale: scale, palette: palette };
}

function getGranularity(resolution: string): { unit: string; interval: moment.Duration } {
  switch (resolution) {
    case 'years':
      return { interval: moment.duration(1, 'year'), unit: 'year' };
    case 'months':
      return { interval: moment.duration(1, 'month'), unit: 'month' };
    case 'weeks':
      return { interval: moment.duration(1, 'week'), unit: 'week' };
    case 'days':
    default:
      return { interval: moment.duration(1, 'day'), unit: 'day' };
  }
}

export enum PositiveNegativeSide {
  POSITIVE,
  NEGATIVE,
}

export function splitPositiveNegativeData(data: BuildChartData[], side: PositiveNegativeSide) {
  return data.map((d) => {
    const newD: BuildChartData = { date: d.date };
    Object.keys(d).forEach((k) => {
      if (k !== 'date') {
        if (d[k] >= 0 && side === PositiveNegativeSide.POSITIVE) {
          newD[k] = d[k];
        } else if (d[k] < 0 && side === PositiveNegativeSide.NEGATIVE) {
          newD[k] = d[k];
        } else {
          newD[k] = 0;
        }
      }
    });
    return newD;
  });
}
