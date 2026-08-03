'use strict';

import empty from '../VisualizationComponents/Empty';
import issueImpact from '../legacy/issue-impact';
import ciBuilds from '../VisualizationComponents/ciBuilds';
import issues from '../VisualizationComponents/issues';
import issueBreakdown from '../VisualizationComponents/issueBreakdown';
import changes from '../VisualizationComponents/changes';
import sprints from '../VisualizationComponents/sprints';
import timeSpent from '../VisualizationComponents/timeSpent';
import vulnerabilityAgeBuckets from '../vulnerabilityTrends/vulnerabilityAgeBuckets';
import vulnerabilityRemediationTimes from '../vulnerabilityTrends/vulnerabilityRemediationTimes';
import vulnerabilityPatchLagSnapshot from '../vulnerabilityTrends/vulnerabilityPatchLag';
import vulnerabilityDirectTransitive from '../vulnerabilityTrends/vulnerabilityDirectTransitive';
import vulnerabilitySeverityDistribution from '../vulnerabilityTrends/vulnerabilitySeverityDistribution';
import outdatedDependencyPercentage from '../vulnerabilityTrends/outdatedDependencyPercentage';
import licenseCompliance from '../vulnerabilityTrends/licenseCompliance';

export default {
  changes,
  issues,
  ciBuilds,
  issueImpact,
  issueBreakdown,
  timeSpent,
  sprints,
  empty,
  vulnerabilityAgeBuckets,
  vulnerabilityRemediationTimes,
  vulnerabilityPatchLagSnapshot,
  vulnerabilityDirectTransitive,
  vulnerabilitySeverityDistribution,
  outdatedDependencyPercentage,
  licenseCompliance,
};
