import React from 'react';
import { setGlobalCurrentFileData } from '../reducer';
import { useSelector } from 'react-redux';
import type { RootState } from '../../../../../../redux';
import type { FileListElementType } from '../../../../../../types/data/fileListType.ts';

export interface SettingsType {
  file: string;
  splitAdditionsDeletions: boolean;
  visualizationStyle: string;
  showSprints: boolean;
  showExtraMetrics: boolean;
}

function FileSelector({
  selectedFile,
  onFileChange,
  dataPluginId,
}: {
  selectedFile: string;
  onFileChange: (file: string) => void;
  dataPluginId?: number;
}) {
  const rawFiles = useSelector((state: RootState) => state.files.fileLists);
  const [searchTerm, setSearchTerm] = React.useState('');

  if (!rawFiles || Object.keys(rawFiles).length === 0) {
    return <div className="alert alert-warning">No files found. Load File Tree first.</div>;
  }

  // Use the specific data plugin's file list when available; fall back to the first entry.
  const files: FileListElementType[] =
    (dataPluginId !== undefined ? rawFiles[dataPluginId] : undefined) ?? (Object.values(rawFiles)[0] as FileListElementType[]);

  if (!files || files.length === 0) {
    return <div className="alert alert-warning">No files found. Load File Tree first.</div>;
  }

  const filteredFiles = files.filter((file) => file.element.path.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="w-full max-w-xs rounded-lg bg-base-200 p-3 shadow mb-1">
      <input
        type="text"
        className="input input-sm w-full mb-2"
        placeholder="Search files..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      <select className="select select-sm w-full" value={selectedFile} onChange={(e) => onFileChange(e.target.value)}>
        {filteredFiles.map((file, index) => (
          <option key={index} value={file.element.path}>
            {file.element.path}
          </option>
        ))}
      </select>
    </div>
  );
}

function Settings(props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void; dataPluginId?: number }) {
  return (
    <>
      <div>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Show Sprints:</span>
          <input
            type="checkbox"
            className="toggle toggle-primary toggle-sm"
            defaultChecked={props.settings.showSprints}
            onChange={(event) =>
              props.setSettings({
                ...props.settings,
                showSprints: event.target.checked,
              })
            }
          />
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Visualization Style:</span>
          <select
            className={'select select-bordered select-xs w-24'}
            defaultValue={props.settings.visualizationStyle}
            onChange={(e) =>
              props.setSettings({
                ...props.settings,
                visualizationStyle: e.target.value,
              })
            }>
            <option value={'curved'}>curved</option>
            <option value={'stepped'}>stepped</option>
            <option value={'linear'}>linear</option>
          </select>
        </label>
        <label className="form-control w-full max-w-xs">
          <div className="label">
            <span className="label-text">File:</span>
          </div>
          <FileSelector
            selectedFile={props.settings.file || ''}
            onFileChange={(file) => {
              setGlobalCurrentFileData(file);
              props.setSettings({
                ...props.settings,
                file: file,
              });
            }}
            dataPluginId={props.dataPluginId}
          />
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Split Additions and Deletions:</span>
          <input
            type="checkbox"
            className="toggle toggle-primary toggle-sm"
            defaultChecked={props.settings.splitAdditionsDeletions}
            onChange={(event) =>
              props.setSettings({
                ...props.settings,
                splitAdditionsDeletions: event.target.checked,
              })
            }
          />
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Show extra Metrics</span>
          <input
            type="checkbox"
            className="toggle toggle-primary toggle-sm"
            defaultChecked={props.settings.showExtraMetrics}
            onChange={(event) =>
              props.setSettings({
                ...props.settings,
                showExtraMetrics: event.target.checked,
              })
            }
          />
        </label>
      </div>
    </>
  );
}

export default Settings;
