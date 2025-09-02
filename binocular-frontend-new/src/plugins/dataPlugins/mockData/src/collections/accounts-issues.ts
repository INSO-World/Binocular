import type {
  DataPluginAccountIssues,
  DataPluginAccountsIssues,
} from "../../../../interfaces/dataPluginInterfaces/dataPluginAccountsIssues.ts";

export default class AccountsIssues implements DataPluginAccountsIssues {
  constructor() {}

  /**
   * Retrieves mock accounts with mock issues
   */
  public async getAll(
    from: string,
    to: string,
  ): Promise<DataPluginAccountIssues[]> {
    return new Promise<DataPluginAccountIssues[]>((resolve) => {
      //Scenario A: Isolated Members in Small Teams
      // @ts-ignore
      const scenario_a = [
        {
          id: "alice",
          login: "alice",
          name: "Alice",
          avatarUrl:
            "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
          url: "https://example.com/alice",
          issues: [
            {
              id: "1",
              iid: 1,
              title: "UI Bug",
              description: "",
              createdAt: "2025-01-01T00:00:00.000Z",
              closedAt: "2025-01-01T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/1",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "2",
              iid: 2,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/2",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "3",
              iid: 3,
              title: "UI Bug",
              description: "",
              createdAt: "2025-01-01T00:00:00.000Z",
              closedAt: "2025-01-01T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/3",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "4",
              iid: 4,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/4",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
          ],
        },
        {
          id: "bob",
          login: "bob",
          name: "Bob",
          avatarUrl:
            "https://ui-avatars.com/api/?name=Bob&background=random&color=fff",
          url: "https://example.com/bob",
          issues: [
            {
              id: "1",
              iid: 1,
              title: "UI Bug",
              description: "",
              createdAt: "2025-01-01T00:00:00.000Z",
              closedAt: "2025-01-01T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/1",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "2",
              iid: 2,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/2",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "3",
              iid: 3,
              title: "UI Bug",
              description: "",
              createdAt: "2025-01-01T00:00:00.000Z",
              closedAt: "2025-01-01T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/3",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "4",
              iid: 4,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/4",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
          ],
        },
        {
          id: "carol",
          login: "carol",
          name: "Carol",
          avatarUrl:
            "https://ui-avatars.com/api/?name=Carol&background=random&color=fff",
          url: "https://example.com/carol",
          issues: [
            {
              id: "2",
              iid: 2,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/2",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "3",
              iid: 3,
              title: "UI Bug",
              description: "",
              createdAt: "2025-01-01T00:00:00.000Z",
              closedAt: "2025-01-01T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/3",
              author: {
                id: "alice",
                login: "alice",
                name: "Alice",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Alice&background=random&color=fff",
                url: "https://example.com/alice",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
            {
              id: "5",
              iid: 5,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/2",
              author: {
                id: "dave",
                login: "dave",
                name: "Dave",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Dave&background=random&color=fff",
                url: "https://example.com/dave",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
          ],
        },
        {
          id: "dave",
          login: "dave",
          name: "Dave",
          avatarUrl:
            "https://ui-avatars.com/api/?name=Dave&background=random&color=fff",
          url: "https://example.com/dave",
          issues: [
            {
              id: "5",
              iid: 5,
              title: "Refactor",
              description: "",
              createdAt: "2025-01-02T00:00:00.000Z",
              closedAt: "2025-01-02T00:00:00.000Z",
              state: "open",
              webUrl: "https://example.com/issues/2",
              author: {
                id: "dave",
                login: "dave",
                name: "Dave",
                avatarUrl:
                  "https://ui-avatars.com/api/?name=Dave&background=random&color=fff",
                url: "https://example.com/dave",
              },
              assignee: null,
              assignees: [],
              notes: [],
            },
          ],
        },
        {
          id: "eve",
          login: "eve",
          name: "Eve",
          avatarUrl:
            "https://ui-avatars.com/api/?name=Eve&background=random&color=fff",
          url: "https://example.com/eve",
          issues: [],
        },
      ];
      resolve(scenario_a);
    });
  }
}
