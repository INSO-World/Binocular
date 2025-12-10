// #v-ifdef PRE_CONFIGURE_DB=='pouchdb'
import { addDataPlugin, LocalDatabaseLoadingState, setLocalDatabaseLoadingState } from '../redux/reducer/settings/settingsReducer.ts';
import type { AppDispatch } from '../redux';
import { PouchDB } from '../plugins/pluginRegistry.ts';

/**
 * Imports when Frontends gets build with arangodb preloaded.
 * These imports are allowed to fail during a normal build.
 */
import branches from '../db_export/branches.json';
import branchesFiles from '../db_export/branches-files.json';
import branchesFilesFiles from '../db_export/branches-files-files.json';
import builds from '../db_export/builds.json';
import commitsCommits from '../db_export/commits-commits.json';
import commitsFiles from '../db_export/commits-files.json';
import commitsBuilds from '../db_export/commits-builds.json';
import commitsFilesUsers from '../db_export/commits-files-users.json';
import commitsModules from '../db_export/commits-modules.json';
import commitsUsers from '../db_export/commits-users.json';
import commits from '../db_export/commits.json';
import files from '../db_export/files.json';
import issuesCommits from '../db_export/issues-commits.json';
import issuesUsers from '../db_export/issues-users.json';
import issues from '../db_export/issues.json';
import modulesFiles from '../db_export/modules-files.json';
import modulesModules from '../db_export/modules-modules.json';
import modules from '../db_export/modules.json';
import users from '../db_export/users.json';
import mergeRequests from '../db_export/mergeRequests.json';
import milestones from '../db_export/milestones.json';
import issuesMilestones from '../db_export/issues-milestones.json';
import mergeRequestsMilestones from '../db_export/mergeRequests-milestones.json';
import accounts from '../db_export/accounts.json';
import issuesAccounts from '../db_export/issues-accounts.json';
import mergeRequestsAccounts from '../db_export/mergeRequests-accounts.json';
import notes from '../db_export/notes.json';
import issuesNotes from '../db_export/issues-notes.json';
import mergeRequestsNotes from '../db_export/mergeRequests-notes.json';
import notesAccounts from '../db_export/notes-accounts.json';
import accountsUsers from '../db_export/accounts-users.json';
import metadata from '../db_export/metadata.json';
import type { JSONObject } from '../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

const dbObjects: { [key: string]: JSONObject[] } = {
  branches: branches,
  'branches-files': branchesFiles,
  'branches-files-files': branchesFilesFiles,
  builds: builds,
  'commits-commits': commitsCommits,
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  'commits-files': commitsFiles,
  'commits-builds': commitsBuilds,
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  'commits-files-users': commitsFilesUsers,
  'commits-modules': commitsModules,
  'commits-users': commitsUsers,
  commits: commits,
  files: files,
  'issues-commits': issuesCommits,
  'issues-users': issuesUsers,
  issues: issues,
  'modules-files': modulesFiles,
  'modules-modules': modulesModules,
  modules: modules,
  users: users,
  mergeRequests: mergeRequests,
  milestones: milestones,
  'issues-milestones': issuesMilestones,
  'mergeRequests-milestones': mergeRequestsMilestones,
  accounts: accounts,
  'issues-accounts': issuesAccounts,
  notes: notes,
  'mergeRequests-accounts': mergeRequestsAccounts,
  'issues-notes': issuesNotes,
  'mergeRequests-notes': mergeRequestsNotes,
  'notes-accounts': notesAccounts,
  'accounts-users': accountsUsers,
};

export default abstract class DatabaseLoaders {
  public static async loadJsonFilesToPouchDB(dispatch: AppDispatch): Promise<void> {
    console.log(`Loading preconfigured PouchDB from ${metadata.namespace} created at ${metadata.createdAt} from ${metadata.type}`);
    dispatch(setLocalDatabaseLoadingState(LocalDatabaseLoadingState.loading));
    return PouchDB.init(undefined, undefined, { name: metadata.namespace, file: undefined, dbObjects: dbObjects }).then(() => {
      dispatch(
        addDataPlugin({
          name: 'PouchDb',
          color: '#8cadfc',
          id: 0,
          isDefault: true,
          parameters: {
            apiKey: undefined,
            endpoint: undefined,
            fileName: metadata.namespace,
            progressUpdate: undefined,
          },
        }),
      );
      dispatch(setLocalDatabaseLoadingState(LocalDatabaseLoadingState.none));
      dispatch({ type: 'REFRESH_PLUGIN', payload: { pluginId: 0 } });
    });
  }
}
// #v-endif
