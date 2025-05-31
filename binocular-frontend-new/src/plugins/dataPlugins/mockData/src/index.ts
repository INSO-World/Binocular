import Commits from "./commits.ts";
import { DataPlugin } from "../../../interfaces/dataPlugin.ts";
import Users from "./users.ts";
import General from "./general.ts";
import Files from "./files.ts";
import Builds from "./builds.ts";
import AccountsIssues from "./accounts-issues.ts";

class MockData implements DataPlugin {
  public name = "Mock Data";
  public description = "Mocked Data for testing purposes.";
  public capabilities = ["authors", "commits", "files"];
  public experimental = false;
  public requirements = {
    apiKey: false,
    endpoint: false,
    file: false,
    progressUpdate: false,
  };
  public commits;
  public builds;
  public users;
  public general;
  public files;
  public accountsIssues;

  constructor() {
    this.commits = new Commits();
    this.builds = new Builds();
    this.users = new Users();
    this.general = new General();
    this.files = new Files();
    this.accountsIssues = new AccountsIssues();
  }

  public async init() {}

  public async clearRemains() {}
}

export default MockData;
