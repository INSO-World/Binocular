export interface DatabaseSettingsType {
  dataPlugins: DatabaseSettingsDataPluginType[];
  defaultDataPluginItemId?: number;
  currID: number;
}

export interface DatabaseSettingsDataPluginType {
  id?: number;
  name: string;
  color: string;
  isDefault?: boolean;
  parameters: {
    apiKey?: string;
    endpoint?: string;
    fileName?: string;
    progressUpdate?: ProgressUpdateConfig;
  };
}

export interface ProgressUpdateConfig {
  useAutomaticUpdate: boolean;
  endpoint?: string;
}
