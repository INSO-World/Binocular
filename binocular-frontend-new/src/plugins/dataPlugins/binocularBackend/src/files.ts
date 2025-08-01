import type {DataPluginFile, DataPluginFiles} from '../../../interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import { GraphQL, traversePages } from './utils.ts';
import { gql } from '@apollo/client';

export default class Files implements DataPluginFiles {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll() {
    console.log(`Getting Files`);
    try {
      const fileList: DataPluginFile[] = [];
      const getFilesPage = () => async (page: number, perPage: number) => {
        const resp = await this.graphQl.client.query({
          query: gql`
            query ($page: Int, $perPage: Int) {
              files(page: $page, perPage: $perPage) {
                count
                page
                perPage
                data {
                  path
                  webUrl
                  maxLength
                }
              }
            }
          `,
          variables: { page, perPage },
        });
        return resp.data.files;
      };

      await traversePages(getFilesPage(), (file: DataPluginFile) => {
        fileList.push(file);
      });
      return fileList;
    } catch (e) {
      console.log(e);
      return [];
    }
  }

  public async getFilenamesForBranch(branchName: string) {
    return await this.graphQl.client
      .query({
        query: gql`
      query{
        branch(branchName: "${branchName}"){
          files{
            data{
              file{
                path
              }
            }
          }
        }
      }
      `,
      })
      .then((result) => {
        return result.data.branch.files.data.map((entry: { file: { path: string } }) => entry.file.path).sort();
      });
  }

  public async getPreviousFilenamesForFilesOnBranch(branchName: string) {
    const result = await this.graphQl.client.query({
      query: gql`
      query{
        branch(branchName: "${branchName}") {
          files {
            data {
              file {
                path
                oldFileNames(branch: "${branchName}") {
                  data {
                    oldFilePath
                    hasThisNameFrom
                    hasThisNameUntil
                  }
                }
              }
            }
          }
        }
      }
      `,
    });

    return result.data.branch.files.data.map((entry: { file: { path: string; oldFileNames: { data: string[] } } }) => {
      return {
        path: entry.file.path,
        previousFileNames: entry.file.oldFileNames.data,
      };
    });
  }
}
