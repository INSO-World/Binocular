import { UniversalSettings } from './unversalSettingsTypes';

// eslint-disable-next-line max-len
import type { VulnerabilityAgeBucketsState } from '../visualizations/vulnerabilityTrends/vulnerabilityAgeBuckets/reducers';
import type { VulnerabilityRemediationTimesState } from '../visualizations/vulnerabilityTrends/vulnerabilityRemediationTimes/reducers';

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
    [key: string]: any; // keep existing visualizations untyped for now
  };
}

export interface DateRange {
  from: string | undefined;
  to: string | undefined;
}
