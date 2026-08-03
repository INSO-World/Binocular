import { UniversalSettings } from './unversalSettingsTypes';

// eslint-disable-next-line max-len
import type { VulnerabilityAgeBucketsState } from '../visualizations/vulnerabilityTrends/vulnerabilityAgeBuckets/reducers';
import type { VulnerabilityRemediationTimesState } from '../visualizations/vulnerabilityTrends/vulnerabilityRemediationTimes/reducers';
import type { VulnerabilityPatchLagState } from '../visualizations/vulnerabilityTrends/vulnerabilityPatchLag/reducers';
import type { VulnerabilityDirectTransitiveState } from '../visualizations/vulnerabilityTrends/vulnerabilityDirectTransitive/reducers';
// eslint-disable-next-line max-len
import type { VulnerabilitySeverityDistributionState } from '../visualizations/vulnerabilityTrends/vulnerabilitySeverityDistribution/reducers';
// eslint-disable-next-line max-len
import type { OutdatedDependencyPercentageState } from '../visualizations/vulnerabilityTrends/outdatedDependencyPercentage/reducers';

export interface GlobalState {
  activeConfigTab: string;
  activeVisualization: string;
  config: any;
  notifications: any;
  progress: any;
  showHelp: boolean;
  universalSettings: UniversalSettings;

  visualizations: {
    vulnerabilityAgeBuckets?: {
      state: VulnerabilityAgeBucketsState;
    };
    vulnerabilityRemediationTimes?: {
      state: VulnerabilityRemediationTimesState;
    };
    vulnerabilityPatchLag?: {
      state: VulnerabilityPatchLagState;
    };
    vulnerabilityDirectTransitive?: {
      state: VulnerabilityDirectTransitiveState;
    };
    vulnerabilitySeverityDistribution?: {
      state: VulnerabilitySeverityDistributionState;
    };
    outdatedDependencyPercentage?: {
      state: OutdatedDependencyPercentageState;
    };
    [key: string]: any; // keep existing visualizations untyped for now
  };
}

export interface DateRange {
  from: string | undefined;
  to: string | undefined;
}
