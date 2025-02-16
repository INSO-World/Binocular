'use strict';

import ChartComponent from './list';
import ConfigComponent from './config.tsx';
import HelpComponent from './help.tsx';
import saga from './sagas';
import reducer from './reducers';

export default {
  id: 'tags',
  label: 'Categorizations',
  saga,
  reducer,
  ChartComponent,
  ConfigComponent,
  HelpComponent,
  usesUniversalSettings: true,
  universalSettingsConfig: { hideExcludeCommitSettings: true, hideMergeCommitSettings: true },
};
