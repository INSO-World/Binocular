import moment from 'moment/moment';
import { ChartData, Palette } from '../chart/chart.tsx';
import { ParametersType } from '../../../../../types/parameters/parametersType.ts';
import chroma from 'chroma-js';
import _ from 'lodash';
import { DataPluginBuild } from '../../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';
import { AuthorType } from '../../../../../types/data/authorType.ts';
import { Properties } from '../interfaces/properties.ts';
import { BuildSettings } from '../settings/settings.tsx';

export function convertToChartData<DataType>(
  builds: DataPluginBuild[],
  props: Properties<BuildSettings, DataType>,
): {
  chartData: ChartData[];
  scale: number[];
  palette: Palette;
} {
  if (!builds || builds.length === 0) {
    return { chartData: [], palette: {}, scale: [] };
  }

  const authorList: AuthorType[] = props.authorList;
  const parameters: ParametersType = props.parameters;
  const splitBuildsPerAuthor: boolean = props.settings.splitBuildsPerAuthor;

  //Sort builds after their build time in case they arnt sorted
  const sortedBuilds = _.clone(builds).sort((c1, c2) => new Date(c1.createdAt).getTime() - new Date(c2.createdAt).getTime());

  const firstTimestamp = sortedBuilds[0].createdAt;
  const lastTimestamp = sortedBuilds[sortedBuilds.length - 1].createdAt;

  const chartData: ChartData[] = [];
  const scale: number[] = [0, 0];
  const palette: Palette = {};
  let returnValue;

  if (sortedBuilds.length > 0) {
    if (splitBuildsPerAuthor) {
      returnValue = getDataByAuthors(parameters, firstTimestamp, lastTimestamp, sortedBuilds, scale, palette, chartData, authorList);
    } else {
      returnValue = getDataByStatus(parameters, firstTimestamp, lastTimestamp, sortedBuilds, scale, palette, chartData);
    }
  } else {
    return { chartData: [], palette: {}, scale: [] };
  }
  // TODO split chartData into positive and negative Data for generalisation
  return returnValue;
}

function getDataByStatus(
  parameters: ParametersType,
  firstTimestamp: string,
  lastTimestamp: string,
  sortedBuilds: DataPluginBuild[],
  scale: number[],
  palette: Palette,
  chartData: ChartData[],
) {
  const data: Array<{ date: number; statsBySortingObject: { [signature: string]: { count: number } } }> = [];
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
    const obj: { date: number; statsBySortingObject: { [signature: string]: { count: number } } } = {
      date: currTimestamp,
      statsBySortingObject: {},
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

      if (buildStatus in obj.statsBySortingObject) {
        obj.statsBySortingObject[buildStatus].count += 1;
      } else {
        obj.statsBySortingObject[buildStatus] = { count: 1 };
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
    const obj: ChartData = { date: build.date };

    for (const status of statuses) {
      obj[status] = 0;
    }
    obj['others'] = 0;

    statuses.forEach((status) => {
      if (status in build.statsBySortingObject) {
        obj[status] = build.statsBySortingObject[status].count;
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

  return { chartData: chartData, scale: scale, palette: palette };
}

function getDataByAuthors(
  parameters: ParametersType,
  firstTimestamp: string,
  lastTimestamp: string,
  sortedBuilds: DataPluginBuild[],
  scale: number[],
  palette: Palette,
  chartData: ChartData[],
  authors: AuthorType[],
) {
  const data: Array<{ date: number; statsBySortingObject: { [signature: string]: { success: number; failed: number } } }> = [];
  //---- STEP 1: AGGREGATE COMMITS GROUPED BY AUTHORS PER TIME INTERVAL ----
  const granularity = getGranularity(parameters.parametersGeneral.granularity);
  const curr = moment(firstTimestamp)
    .startOf(granularity.unit as moment.unitOfTime.StartOf)
    .subtract(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
  const end = moment(lastTimestamp)
    .endOf(granularity.unit as moment.unitOfTime.StartOf)
    .add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
  const next = moment(curr).add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity);
  const totalBuildsPerAuthor: { [signature: string]: number } = {};
  for (
    let i = 0;
    curr.isSameOrBefore(end);
    curr.add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity),
      next.add(1, <moment.unitOfTime.DurationConstructor>parameters.parametersGeneral.granularity)
  ) {
    //Iterate through time buckets
    const currTimestamp = curr.toDate().getTime();
    const nextTimestamp = next.toDate().getTime();
    const obj: { date: number; statsBySortingObject: { [signature: string]: { success: number; failed: number } } } = {
      date: currTimestamp,
      statsBySortingObject: {},
    }; //Save date of time bucket, create object
    for (; i < sortedBuilds.length && Date.parse(sortedBuilds[i].createdAt) < nextTimestamp; i++) {
      //Iterate through commits that fall into this time bucket
      const commitAuthor = sortedBuilds[i].user?.id;
      if (totalBuildsPerAuthor[commitAuthor] === null) {
        totalBuildsPerAuthor[commitAuthor] = 0;
      }
      totalBuildsPerAuthor[commitAuthor] += 1;
      let success = 0;
      let failed = 0;
      if (sortedBuilds[i].status === 'success') {
        success += 1;
      } else {
        failed += 1;
      }
      if (
        commitAuthor in obj.statsBySortingObject //If object is already in statsBySortingObject, add to previous values
      ) {
        obj.statsBySortingObject[commitAuthor] = {
          success: obj.statsBySortingObject[commitAuthor].success + success,
          failed: obj.statsBySortingObject[commitAuthor].failed + failed,
        };
      } else {
        //Else create new values
        obj.statsBySortingObject[commitAuthor] = { success: success, failed: failed };
      }
    }
    data.push(obj);
  }

  //---- STEP 2: CONSTRUCT CHART DATA FROM AGGREGATED COMMITS ----
  palette['Successful builds others'] = { main: '#555555', secondary: '#777777' };
  palette['Failed builds others'] = { main: '#AAAAAA', secondary: '#CCCCCC' };
  data.forEach((commit) => {
    //commit has structure {date, statsByAuthor: {}} (see next line)}
    const obj: ChartData = { date: commit.date };

    for (const author of authors) {
      palette['Successful builds ' + (author.displayName || author.user.gitSignature)] = {
        main: chroma(author.color.main).hex(),
        secondary: chroma(author.color.secondary).hex(),
      };
      palette['Failed builds ' + (author.displayName || author.user.gitSignature)] = {
        main: chroma(author.color.main).darken(0.5).hex(),
        secondary: chroma(author.color.secondary).darken(0.5).hex(),
      };
      obj['Successful builds ' + (author.displayName || author.user.gitSignature)] = 0.001;
      obj['Failed builds ' + (author.displayName || author.user.gitSignature)] = -0.001; //-0.001 for stack layout to realize it belongs on the bottom
    }
    obj['Successful builds others'] = 0;
    obj['Failed builds others'] = -0.001;

    authors.forEach((author) => {
      if (!author.selected) return;
      const name =
        author.parent === -1
          ? author.displayName || author.user.gitSignature
          : author.parent === 0
            ? 'others'
            : authors.filter((a) => a.id === author.parent)[0].user.gitSignature;

      if (author.user.id in commit.statsBySortingObject) {
        //Insert number of changes with the author name as key,
        //statsByAuthor has structure {{authorName: {count, additions, deletions, changes}}, ...}
        if ('Successful builds ' + name in obj && 'Failed builds ' + name in obj) {
          obj['Successful builds ' + name] += commit.statsBySortingObject[author.user.id].success;
          //-0.001 for stack layout to realize it belongs on the bottom
          obj['Failed builds ' + name] += commit.statsBySortingObject[author.user.id].failed * -1 - 0.001;
        } else {
          obj['Successful builds ' + name] = commit.statsBySortingObject[author.user.id].success;
          //-0.001 for stack layout to realize it belongs on the bottom
          obj['Failed builds ' + name] = commit.statsBySortingObject[author.user.id].failed * -1 - 0.001;
        }
      }
    });

    chartData.push(obj); //Add object to list of objects
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
        if (key.includes('Successful builds ')) {
          positiveTotals += dataPoint[key];
        } else if (key.includes('Failed builds ')) {
          negativeTotals += dataPoint[key];
        } else {
          positiveTotals += dataPoint[key];
        }
      });
    if (positiveTotals > scale[1]) {
      scale[1] = positiveTotals;
    }
    if (negativeTotals < scale[0]) {
      scale[0] = negativeTotals;
    }
  });
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

export function splitPositiveNegativeData(data: ChartData[], side: PositiveNegativeSide) {
  return data.map((d) => {
    const newD: ChartData = { date: d.date };
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
