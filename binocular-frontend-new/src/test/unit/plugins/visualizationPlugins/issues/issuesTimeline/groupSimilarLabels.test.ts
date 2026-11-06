import { describe, it, expect } from 'vitest';
import {
  groupSimilarLabels,
  defaultLabelGroupId,
} from '../../../../../../plugins/visualizationPlugins/issues/issuesTimeline/src/chart/helper/groupSimilarLabels';

describe('groupSimilarLabels', () => {
  it('U7.1 returns an empty map for empty input', () => {
    expect(groupSimilarLabels([])).toEqual(new Map());
  });

  it('U7.2 treats an isolated string (no close neighbors) as noise (group 0 → map key -1)', () => {
    // "foo" has no neighbors within epsilon=1 among single-item input
    const result = groupSimilarLabels(['foo'], 1, 2);
    // noise points get label 0, which maps to key (label - 1) = -1
    expect(result.has(defaultLabelGroupId)).toBe(true);
    expect(result.get(defaultLabelGroupId)).toContain('foo');
  });

  it('U7.3 groups closely related strings into the same cluster', () => {
    // "bug" and "Bug" have distance 1
    const result = groupSimilarLabels(['bug', 'Bug'], 1, 2);
    // They should both land in one cluster (not noise)
    const clusters = Array.from(result.entries()).filter(([k]) => k !== defaultLabelGroupId);
    expect(clusters).toHaveLength(1);
    expect(clusters[0][1]).toContain('bug');
    expect(clusters[0][1]).toContain('Bug');
  });

  it('U7.4 produces separate clusters for clearly different strings', () => {
    // "bug" and "feature" are far apart; both sets form their own clusters with minPoints=1
    const result = groupSimilarLabels(['bug', 'Bug', 'feature', 'Feature'], 1, 2);
    const clusters = Array.from(result.entries()).filter(([k]) => k !== defaultLabelGroupId);
    expect(clusters.length).toBeGreaterThanOrEqual(2);
  });

  it('U7.5 all strings cluster together when epsilon is very large', () => {
    const strings = ['a', 'bbb', 'ccccc'];
    const result = groupSimilarLabels(strings, 100, 1);
    const clusters = Array.from(result.entries()).filter(([k]) => k !== defaultLabelGroupId);
    // Everything reachable from everything → one cluster
    expect(clusters).toHaveLength(1);
    const allValues = clusters.flatMap(([, v]) => v);
    expect(allValues.sort()).toEqual(strings.sort());
  });

  it('U7.6 uses default epsilon=3 and minPoints=2 when not specified', () => {
    // "bug" and "bugs" have distance 1, "feature" is isolated
    const result = groupSimilarLabels(['bug', 'bugs', 'feature']);
    const bugCluster = Array.from(result.values()).find((v) => v.includes('bug'));
    expect(bugCluster).toContain('bugs');
  });
});
