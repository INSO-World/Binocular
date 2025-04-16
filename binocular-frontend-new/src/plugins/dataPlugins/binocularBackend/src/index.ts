import Commits from "./commits.ts";
import { DataPlugin } from "../../../interfaces/dataPlugin.ts";
import General from "./general.ts";
import Files from "./files.ts";
import Users from "./users.ts";
import { FileConfig } from "../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts";
import { ProgressUpdateConfig } from "../../../../types/settings/databaseSettingsType.ts";
import Builds from "./builds.ts";
import AccountsIssues from "./accounts-issues.ts";

class BinocularBackend implements DataPlugin {
  public name = "Binocular Backend";
  public description = "Connection to the Binocular GraphQL Backend.";
  public capabilities = ["authors", "commits", "files"];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: true,
    file: false,
    progressUpdate: true,
  };
  public commits;
  public builds;
  public users;
  public general;
  public files;
  public accountsIssues;

  constructor() {
    this.commits = new Commits("/graphQl");
    this.builds = new Builds("/graphQl");
    this.users = new Users("/graphQl");
    this.general = new General("/graphQl", undefined);
    this.files = new Files("/graphQl");
    this.accountsIssues = new AccountsIssues("graphQL");
  }

  // eslint-disable-next-line @typescript-eslint/require-await
  public async init(
    apiKey: string | undefined,
    endpoint: string | undefined,
    _file: FileConfig | undefined,
    progressUpdateConfig: ProgressUpdateConfig | undefined,
  ) {
    console.log(
      `Init Binocular Backend with ApiKey: ${apiKey} and Endpoint ${endpoint}`,
    );
    if (endpoint === undefined || endpoint.length === 0) {
      endpoint = "/graphQl";
    }
    this.commits = new Commits(endpoint);
    this.builds = new Builds(endpoint);
    this.users = new Users(endpoint);
    this.general = new General(endpoint, progressUpdateConfig);
    this.files = new Files(endpoint);
    this.accountsIssues = new AccountsIssues(endpoint);
  }

  public async clearRemains() {}
}

export default BinocularBackend;
