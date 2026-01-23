import { UniversalSettings } from './unversalSettingsTypes';

// eslint-disable-next-line max-len
import type { VulnerabilityAgeBucketsState } from './../visualizations/vulnerabilityTrends/vulnerabilityAgeBuckets/reducers/vulnerabilityAgeBucketsSlice';

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
    [key: string]: any; // keep existing visualizations untyped for now
  };
}

export interface DateRange {
  from: string | undefined;
  to: string | undefined;
}
