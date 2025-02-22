export interface DataPluginJacocoReports {
  getAll: (from: string, to: string, sort: string) => Promise<DataPluginJacocoReport[]>;
}

export interface DataPluginJacocoReport {
  id: number;
  created_at: string;
  xmlContent: string;
}
