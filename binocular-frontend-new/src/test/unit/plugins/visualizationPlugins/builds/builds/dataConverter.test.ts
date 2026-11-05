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

  it('U35.6 unknown status (e.g. pending) is mapped to others — pending key never appears in chartData', () => {
    const build = makeBuild({ status: 'pending' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    // Unknown statuses are renamed to 'others' in step 1, so 'pending' never appears as a chart key
    const allKeys = result.chartData.flatMap((d) => Object.keys(d));
    expect(allKeys).not.toContain('pending');
    // 'others' key is always present (initialized to 0)
    expect(allKeys).toContain('others');
  });

  it('U35.7 build with status failed has a negative chart value', () => {
    const build = makeBuild({ status: 'failed' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    const bucket = result.chartData.find((d) => d['failed'] < 0);
    expect(bucket).toBeDefined();
  });

  it('U35.8 build with status cancelled has a negative chart value', () => {
    const build = makeBuild({ status: 'cancelled' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    const bucket = result.chartData.find((d) => d['cancelled'] < 0);
    expect(bucket).toBeDefined();
  });

  it('U35.9 build with status success has a positive chart value', () => {
    const build = makeBuild({ status: 'success' });
    const result = convertToChartData([build], makeProps(defaultSettings));
    const bucket = result.chartData.find((d) => d['success'] > 0);
    expect(bucket).toBeDefined();
  });

  it('U35.10 author with selected=false — no data for that author in splitBuildsPerAuthor mode', () => {
    const settings: BuildSettings = { ...defaultSettings, splitBuildsPerAuthor: true };
    const author: AuthorType = {
      id: 1,
      parent: -1,
      selected: false,
      color: { main: '#ff0000', secondary: '#ff0000aa' },
      user: { id: 'u1', gitSignature: 'Alice', account: null },
    };
    const build = makeBuild({ status: 'success', user: { id: 'u1', gitSignature: 'Alice', account: null } });
    const result = convertToChartData([build], makeProps(settings, [author]));
    // Author is not selected so their builds should not accumulate beyond the 0.001 placeholder
    const successAlice = result.chartData.reduce((sum, d) => sum + (d['Successful builds Alice'] ?? 0), 0);
    // 0.001 is the placeholder for un-accumulated authors; no real successes should be added
    expect(successAlice).toBeLessThanOrEqual(0.001 * result.chartData.length);
  });
});
