import { describe, it, expect } from 'vitest';
import { convertToChartData } from '../../../../../../plugins/visualizationPlugins/builds/builds/src/utilities/dataConverter';
import type { DataPluginBuild } from '../../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginBuilds';
import type { BuildSettings } from '../../../../../../plugins/visualizationPlugins/builds/builds/src/settings/settings';
import type { AuthorType } from '../../../../../../types/data/authorType';
import type { DataPlugin } from '../../../../../../plugins/interfaces/dataPlugin';
import type { Store } from '@reduxjs/toolkit';

function makeProps(settings: BuildSettings, authorList: AuthorType[] = []) {
  return {
    settings,
    parameters: {
      parametersGeneral: { granularity: 'months', excludeMergeCommits: false },
      parametersDateRange: { from: '', to: '' },
    },
    authorList,
    fileList: [],
    sprintList: [],
    dataConnection: {} as unknown as DataPlugin,
    chartContainerRef: { current: null },
    store: {} as unknown as Store,
  };
}

const defaultSettings: BuildSettings = {
  splitBuildsPerAuthor: false,
  visualizationStyle: 'curved',
  showSprints: false,
};

function makeBuild(overrides: Partial<DataPluginBuild> = {}): DataPluginBuild {
  return {
    id: 1,
    committedAt: '2023-06-15T00:00:00Z',
    createdAt: '2023-06-15T00:00:00Z',
    duration: '60',
    finishedAt: '2023-06-15T00:01:00Z',
    jobs: [],
    startedAt: '2023-06-15T00:00:00Z',
    status: 'success',
    updatedAt: '2023-06-15T00:01:00Z',
    user: { id: 'u1', gitSignature: 'Alice', account: null },
    userFullName: 'Alice',
    ...overrides,
  };
}

describe('convertToChartData (builds)', () => {
  it('U35.1 returns empty result for empty array', () => {
    const result = convertToChartData([], makeProps(defaultSettings));
    expect(result.chartData).toEqual([]);
    expect(result.palette).toEqual({});
    expect(result.scale).toEqual([]);
  });

  it('U35.2 success build lands in correct time bucket', () => {
    const build = makeBuild({ status: 'success', createdAt: '2023-06-15T00:00:00Z' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    expect(result.chartData.length).toBeGreaterThan(0);
    const bucketWithSuccess = result.chartData.find((d) => d['success'] > 0);
    expect(bucketWithSuccess).toBeDefined();
  });

  it('U35.3 splitBuildsPerAuthor:false — status keys appear in chartData', () => {
    const build = makeBuild({ status: 'failed' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    // failed builds are negated
    const bucket = result.chartData.find((d) => d['failed'] < 0);
    expect(bucket).toBeDefined();
  });

  it('U35.4 splitBuildsPerAuthor:true — author-prefixed keys appear in chartData', () => {
    const settings: BuildSettings = { ...defaultSettings, splitBuildsPerAuthor: true };
    const build = makeBuild({ status: 'success' });
    const result = convertToChartData([build], makeProps(settings, []));
    // With empty authorList, should have 'Successful builds others' and 'Failed builds others'
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys.some((k) => k.includes('builds'))).toBe(true);
  });

  it('U35.5 scale[1] positive for success builds', () => {
    const build = makeBuild({ status: 'success' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    expect(result.scale[1]).toBeGreaterThan(0);
  });
});
