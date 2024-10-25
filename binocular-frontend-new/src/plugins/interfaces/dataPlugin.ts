import { DataPluginGeneral } from './dataPluginInterfaces/dataPluginGeneral.ts';
import { DataPluginCommits } from './dataPluginInterfaces/dataPluginCommits.ts';
import { DataPluginUsers } from './dataPluginInterfaces/dataPluginUsers.ts';
import { DataPluginFiles, FileConfig } from './dataPluginInterfaces/dataPluginFiles.ts';
import { ProgressUpdateConfig } from '../../types/settings/databaseSettingsType.ts';

export interface DataPlugin {
  name: string;
  description: string;
  general: DataPluginGeneral;
  commits: DataPluginCommits;
  users: DataPluginUsers;
  files: DataPluginFiles;
  capabilities: string[];
  experimental: boolean;
  requirements: { apiKey: boolean; endpoint: boolean; file: boolean; progressUpdate: boolean };
  init: (
    apiKey: string | undefined,
    endpoint: string | undefined,
    fileConfig: FileConfig | undefined,
    progressUpdateConfig: ProgressUpdateConfig | undefined,
  ) => Promise<void>;
  clearRemains: () => Promise<void>;
}
