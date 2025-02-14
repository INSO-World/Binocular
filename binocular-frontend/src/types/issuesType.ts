export interface Issue {
  title: string;
  webUrl: string;
  labels: Label[];
}

export interface Label {
  name: string;
}
