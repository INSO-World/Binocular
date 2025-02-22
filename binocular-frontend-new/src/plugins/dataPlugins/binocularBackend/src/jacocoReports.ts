import { GraphQL, traversePages } from './utils';
import { gql } from '@apollo/client';
import { DataPluginJacocoReport, DataPluginJacocoReports } from '../../../interfaces/dataPluginInterfaces/dataPluginArtifacts.ts';

export default class JacocoReports implements DataPluginJacocoReports {
  private graphQl;

  constructor(endpoint: string) {
    this.graphQl = new GraphQL(endpoint);
  }

  public async getAll(from: string, to: string, sort: string): Promise<DataPluginJacocoReport[]> {
    console.log(`Getting jacoco reports from ${from} to ${to}`);
    const jacocoReportsList: DataPluginJacocoReport[] = [];
    const getArtifactsPage = () => async (page: number, perPage: number) => {
      const resp = await this.graphQl.client.query({
        query: gql`
          query ($page: Int, $perPage: Int, $from: Timestamp, $to: Timestamp, $sort: Sort) {
            jacocoReports(page: $page, perPage: $perPage, from: $from, to: $to, sort: $sort) {
              count
              page
              perPage
              data {
                id
                created_at
                xmlContent
              }
            }
          }
        `,
        variables: { page, perPage, from, to, sort },
      });
      return resp.data.jacocoReports;
    };

    await traversePages(getArtifactsPage(), (jacocoReport: DataPluginJacocoReport) => {
      jacocoReportsList.push(jacocoReport);
    });
    return jacocoReportsList;
  }
}
