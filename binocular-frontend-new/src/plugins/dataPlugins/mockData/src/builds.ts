import { DataPluginBuild, DataPluginBuilds } from '../../../interfaces/dataPluginInterfaces/dataPluginBuilds.ts';

export default class Builds implements DataPluginBuilds {
  constructor() {}

  public async getAll(from: string, to: string) {
    console.log(`Getting Builds from ${from} to ${to}`);
    return new Promise<DataPluginBuild[]>((resolve) => {
      const builds: DataPluginBuild[] = [
        {
          committedAt: '2024-06-01T12:00:00.000Z',
          createdAt: '2024-06-01T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-06-01T12:00:00.000Z',
          id: 1,
          jobs: [
            {
              id: '1',
              name: 'tester',
              status: 'failure',
              stage: 'failure',
              createdAt: '2024-06-01T12:00:00.000Z',
              finishedAt: '2024-06-01T12:00:00.000Z',
            },
          ],
          startedAt: '2024-06-01T12:00:00.000Z',
          status: 'failed',
          updatedAt: '2024-06-01T12:00:00.000Z',
          user: { id: '1', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-06-02T12:00:00.000Z',
          createdAt: '2024-06-02T12:00:00.000Z',
          duration: '12',
          finishedAt: '2024-06-02T12:00:00.000Z',
          id: 2,
          jobs: [
            {
              id: '2',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-06-02T12:00:00.000Z',
              finishedAt: '2024-06-02T12:00:00.000Z',
            },
          ],
          startedAt: '2024-06-02T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-06-02T12:00:00.000Z',
          user: { id: '1', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-06-10T12:00:00.000Z',
          createdAt: '2024-06-10T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-06-10T12:00:00.000Z',
          id: 3,
          jobs: [
            {
              id: '3',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-06-10T12:00:00.000Z',
              finishedAt: '2024-06-10T12:00:00.000Z',
            },
          ],
          startedAt: '2024-06-10T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-06-10T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-06-11T12:00:00.000Z',
          createdAt: '2024-06-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-06-11T12:00:00.000Z',
          id: 4,
          jobs: [
            {
              id: '4',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-06-11T12:00:00.000Z',
              finishedAt: '2024-06-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-06-11T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-06-11T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-07-10T12:00:00.000Z',
          createdAt: '2024-07-10T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-07-10T12:00:00.000Z',
          id: 5,
          jobs: [
            {
              id: '5',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-07-10T12:00:00.000Z',
              finishedAt: '2024-07-10T12:00:00.000Z',
            },
          ],
          startedAt: '2024-07-10T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-07-10T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-07-11T12:00:00.000Z',
          createdAt: '2024-07-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-07-11T12:00:00.000Z',
          id: 6,
          jobs: [
            {
              id: '6',
              name: 'failure',
              status: 'failure',
              stage: 'failure',
              createdAt: '2024-07-11T12:00:00.000Z',
              finishedAt: '2024-07-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-07-11T12:00:00.000Z',
          status: 'failed',
          updatedAt: '2024-07-11T12:00:00.000Z',
          user: { id: '0', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-07-11T12:00:00.000Z',
          createdAt: '2024-07-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-07-11T12:00:00.000Z',
          id: 7,
          jobs: [
            {
              id: '7',
              name: 'failure',
              status: 'failure',
              stage: 'failure',
              createdAt: '2024-07-11T12:00:00.000Z',
              finishedAt: '2024-07-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-07-11T12:00:00.000Z',
          status: 'failed',
          updatedAt: '2024-07-11T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-08-11T12:00:00.000Z',
          createdAt: '2024-08-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-08-11T12:00:00.000Z',
          id: 8,
          jobs: [
            {
              id: '8',
              name: 'failure',
              status: 'failure',
              stage: 'failure',
              createdAt: '2024-08-11T12:00:00.000Z',
              finishedAt: '2024-08-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-08-11T12:00:00.000Z',
          status: 'failed',
          updatedAt: '2024-08-11T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-08-11T12:00:00.000Z',
          createdAt: '2024-08-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-08-11T12:00:00.000Z',
          id: 9,
          jobs: [
            {
              id: '9',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-08-11T12:00:00.000Z',
              finishedAt: '2024-08-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-08-11T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-08-11T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-08-11T12:00:00.000Z',
          createdAt: '2024-08-11T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-08-11T12:00:00.000Z',
          id: 10,
          jobs: [
            {
              id: '10',
              name: 'success',
              status: 'success',
              stage: 'success',
              createdAt: '2024-08-11T12:00:00.000Z',
              finishedAt: '2024-08-11T12:00:00.000Z',
            },
          ],
          startedAt: '2024-08-11T12:00:00.000Z',
          status: 'success',
          updatedAt: '2024-08-11T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-07-12T12:00:00.000Z',
          createdAt: '2024-07-12T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-07-12T12:00:00.000Z',
          id: 11,
          jobs: [
            {
              id: '11',
              name: 'cancelled',
              status: 'cancelled',
              stage: 'cancelled',
              createdAt: '2024-07-12T12:00:00.000Z',
              finishedAt: '2024-07-12T12:00:00.000Z',
            },
          ],
          startedAt: '2024-07-12T12:00:00.000Z',
          status: 'cancelled',
          updatedAt: '2024-07-12T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-08-13T12:00:00.000Z',
          createdAt: '2024-08-13T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-08-13T12:00:00.000Z',
          id: 12,
          jobs: [
            {
              id: '12',
              name: 'cancelled',
              status: 'cancelled',
              stage: 'cancelled',
              createdAt: '2024-08-13T12:00:00.000Z',
              finishedAt: '2024-08-13T12:00:00.000Z',
            },
          ],
          startedAt: '2024-08-13T12:00:00.000Z',
          status: 'cancelled',
          updatedAt: '2024-08-13T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
        {
          committedAt: '2024-08-14T12:00:00.000Z',
          createdAt: '2024-08-14T12:00:00.000Z',
          duration: '10',
          finishedAt: '2024-08-14T12:00:00.000Z',
          id: 13,
          jobs: [
            {
              id: '13',
              name: 'cancelled',
              status: 'cancelled',
              stage: 'cancelled',
              createdAt: '2024-08-14T12:00:00.000Z',
              finishedAt: '2024-08-14T12:00:00.000Z',
            },
          ],
          startedAt: '2024-08-14T12:00:00.000Z',
          status: 'cancelled',
          updatedAt: '2024-08-14T12:00:00.000Z',
          user: { id: '2', gitSignature: 'Tester tester@email' },
          userFullName: 'Tester',
        },
      ];
      resolve(builds);
    });
  }
}