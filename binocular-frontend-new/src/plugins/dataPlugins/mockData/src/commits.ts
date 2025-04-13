import { DataPluginCommit, DataPluginCommits } from '../../../interfaces/dataPluginInterfaces/dataPluginCommits.ts';

export default class Commits implements DataPluginCommits {
  constructor() {}

  public async getAll(from: string, to: string) {
    console.log(`Getting Commits from ${from} to ${to}`);
    return new Promise<DataPluginCommit[]>((resolve) => {
    const commits: DataPluginCommit[] = [
    {
    sha: '0000000001',
    shortSha: '00001',
    messageHeader: 'Commit 1',
    message: 'This is the first Commit',
    user: {
    id: '1',
    gitSignature: 'tester@github.com',
    },
    branch: 'main',
    date: '2024-06-01T12:00:00.000Z', // Matches build id 1
    parents: [],
    webUrl: 'www.github.com',
    stats: { additions: 5, deletions: 0 },
    },
    {
    sha: '0000000002',
    shortSha: '00002',
    messageHeader: 'Commit 2',
    message: 'This is the second Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-06-02T12:00:00.000Z', // Matches build id 2
    parents: ['0000000001'],
    webUrl: 'www.github.com',
    stats: { additions: 10, deletions: 20 },
    },
    {
    sha: '0000000003',
    shortSha: '00003',
    messageHeader: 'Commit 3',
    message: 'This is the third Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-06-10T12:00:00.000Z', // Matches build id 3
    parents: ['0000000002'],
    webUrl: 'www.github.com',
    stats: { additions: 2, deletions: 5 },
    },
    {
    sha: '0000000004',
    shortSha: '00004',
    messageHeader: 'Commit 4',
    message: 'This is the fourth Commit',
    user: {
    id: '1',
    gitSignature: 'tester@github.com',
    },
    branch: 'main',
    date: '2024-06-11T12:00:00.000Z', // Matches build id 4
    parents: ['0000000003'],
    webUrl: 'www.github.com',
    stats: { additions: 20, deletions: 0 },
    },
    {
    sha: '0000000005',
    shortSha: '00005',
    messageHeader: 'Commit 5',
    message: 'This is the fifth Commit',
    user: {
    id: '1',
    gitSignature: 'tester@github.com',
    },
    branch: 'main',
    date: '2024-07-10T12:00:00.000Z', // Matches build id 5
    parents: ['0000000004'],
    webUrl: 'www.github.com',
    stats: { additions: 6, deletions: 10 },
    },
    {
    sha: '0000000006',
    shortSha: '00006',
    messageHeader: 'Commit 6',
    message: 'This is the sixth Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-07-11T12:00:00.000Z', // Matches build id 6 and 7
    parents: ['0000000005'],
    webUrl: 'www.github.com',
    stats: { additions: 15, deletions: 3 },
    },
    {
    sha: '0000000007',
    shortSha: '00007',
    messageHeader: 'Commit 7',
    message: 'This is the seventh Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-07-12T12:00:00.000Z', // Matches build id 11
    parents: ['0000000006'],
    webUrl: 'www.github.com',
    stats: { additions: 8, deletions: 4 },
    },
    {
    sha: '0000000008',
    shortSha: '00008',
    messageHeader: 'Commit 8',
    message: 'This is the eighth Commit',
    user: {
    id: '1',
    gitSignature: 'tester@github.com',
    },
    branch: 'main',
    date: '2024-08-11T12:00:00.000Z', // Matches build id 8, 9, and 10
    parents: ['0000000007'],
    webUrl: 'www.github.com',
    stats: { additions: 12, deletions: 7 },
    },
    {
    sha: '0000000009',
    shortSha: '00009',
    messageHeader: 'Commit 9',
    message: 'This is the ninth Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-08-13T12:00:00.000Z', // Matches build id 12
    parents: ['0000000008'],
    webUrl: 'www.github.com',
    stats: { additions: 5, deletions: 2 },
    },
    {
    sha: '0000000010',
    shortSha: '00010',
    messageHeader: 'Commit 10',
    message: 'This is the tenth Commit',
    user: {
    id: '1',
    gitSignature: 'tester@github.com',
    },
    branch: 'main',
    date: '2024-08-14T12:00:00.000Z', // Matches build id 13
    parents: ['0000000009'],
    webUrl: 'www.github.com',
    stats: { additions: 9, deletions: 1 },
    },
    // Additional commit that doesn't match any build
    {
    sha: '0000000011',
    shortSha: '00011',
    messageHeader: 'Commit 11',
    message: 'This is the eleventh Commit',
    user: {
    id: '2',
    gitSignature: 'tester2@github.com',
    },
    branch: 'main',
    date: '2024-08-20T12:00:00.000Z', // No matching build
    parents: ['0000000010'],
    webUrl: 'www.github.com',
    stats: { additions: 3, deletions: 3 },
    },
    ];
    resolve(commits);
    });
  }
}