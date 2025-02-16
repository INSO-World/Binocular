export interface CommitMetadata {
  id: string;
  sha: string;
  message: string;
  branch: string;
  date: string;
  webUrl: string;
  diff: Array<{
    id: string;
    edgeId: string;
    sha: string;
    message: string;
    branch: string;
    date: string;
    webUrl: string;
    files: Array<
      | {
          oldFilePath: string;
          newFilePath: string;
          status: 'added' | 'modified' | 'deleted' | 'renamed';
          hunks: Array<{
            oldStart: number;
            oldLines: number;
            newStart: number;
            newLines: number;
            lines: Array<{
              content: string;
              origin: '+' | '-' | ' ';
            }>;
          }>;
        }
      | never
    >;
  }>;
}
