import { DataPluginGeneral } from './dataPluginInterfaces/dataPluginGeneral.ts';
import { DataPluginCommits } from './dataPluginInterfaces/dataPluginCommits.ts';
import { DataPluginUsers } from './dataPluginInterfaces/dataPluginUsers.ts';
import { DataPluginBuilds } from './dataPluginInterfaces/dataPluginBuilds.ts';
import { DataPluginFiles, FileConfig } from './dataPluginInterfaces/dataPluginFiles.ts';
import { ProgressUpdateConfig } from '../../types/settings/databaseSettingsType.ts';
import { DataPluginIssues } from './dataPluginInterfaces/dataPluginIssues.ts';
import { DataPluginNotes } from './dataPluginInterfaces/dataPluginNotes.ts';
import { DataPluginAccounts } from './dataPluginInterfaces/dataPluginAccounts.ts';
import { DataPluginBranches } from './dataPluginInterfaces/dataPluginBranches.ts';

export interface DataPlugin {
  name: string;
  description: string;
  general: DataPluginGeneral;
  commits: DataPluginCommits;
  builds: DataPluginBuilds;
  issues: DataPluginIssues;
  notes: DataPluginNotes;
  users: DataPluginUsers;
  accounts: DataPluginAccounts;
  files: DataPluginFiles;
  branches?: DataPluginBranches;
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
