import { useSelector } from 'react-redux';
import { useAppDispatch, type AppDispatch, type RootState } from '../../redux';
import { ExportType, setExportData, setExportDataType, setExportLoading } from '../../redux/reducer/export/exportReducer.ts';
import dataExportStyles from './dataExport/dataExport.module.scss';
function ViewIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="h-4 w-4">
      <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8z M1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z" />
      <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5z M4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z" />
    </svg>
  );
}

function DownloadIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="h-4 w-4">
      <path d="M8 2a.75.75 0 0 1 .75.75v6.69l1.72-1.72a.75.75 0 1 1 1.06 1.06l-3 3a.75.75 0 0 1-1.06 0l-3-3a.75.75 0 1 1 1.06-1.06l1.72 1.72V2.75A.75.75 0 0 1 8 2Z" />
      <path d="M3.5 9.75a.75.75 0 0 0-1.5 0v1.5A2.75 2.75 0 0 0 4.75 14h6.5A2.75 2.75 0 0 0 14 11.25v-1.5a.75.75 0 0 0-1.5 0v1.5c0 .69-.56 1.25-1.25 1.25h-6.5c-.69 0-1.25-.56-1.25-1.25v-1.5Z" />
    </svg>
  );
}
import type { DatabaseSettingsDataPluginType } from '../../types/settings/databaseSettingsType.ts';
import { useEffect, useState } from 'react';
import DataPluginStorage from '../../utils/dataPluginStorage.ts';
import type { JSONObject } from '../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import { downloadExportCompressed } from '../../plugins/utils/export.ts';

const emptyData = {
  accounts: [] as JSONObject[],
  branches: [] as JSONObject[],
  builds: [] as JSONObject[],
  commits: [] as JSONObject[],
  files: [] as JSONObject[],
  issues: [] as JSONObject[],
  mergeRequests: [] as JSONObject[],
  milestones: [] as JSONObject[],
  modules: [] as JSONObject[],
  notes: [] as JSONObject[],
  users: [] as JSONObject[],
  'accounts-users': [] as JSONObject[],
  'branches-files': [] as JSONObject[],
  'branches-files-files': [] as JSONObject[],
  'commits-builds': [] as JSONObject[],
  'commits-commits': [] as JSONObject[],
  'commits-files': [] as JSONObject[],
  'commits-files-users': [] as JSONObject[],
  'commits-modules': [] as JSONObject[],
  'commits-users': [] as JSONObject[],
  'issues-accounts': [] as JSONObject[],
  'issues-commits': [] as JSONObject[],
  'issues-milestones': [] as JSONObject[],
  'issues-notes': [] as JSONObject[],
  'issues-users': [] as JSONObject[],
  'mergeRequests-accounts': [] as JSONObject[],
  'mergeRequests-milestones': [] as JSONObject[],
  'mergeRequests-notes': [] as JSONObject[],
  'modules-files': [] as JSONObject[],
  'modules-modules': [] as JSONObject[],
  'notes-accounts': [] as JSONObject[],
};

type ExportDataType = typeof emptyData;

const allItemNames = [...Object.keys(emptyData)];

const PAGE_SIZE_OPTIONS = [10, 25, 50, 100];

function ExportDialog() {
  const dispatch: AppDispatch = useAppDispatch();

  const exportType = useSelector((state: RootState) => state.export.exportType);
  const exportSVGData = useSelector((state: RootState) => state.export.exportSVGData);
  const exportName = useSelector((state: RootState) => state.export.exportName);
  const loading = useSelector((state: RootState) => state.export.exportLoading);
  const exportDataType = useSelector((state: RootState) => state.export.exportDataType);
  const exportData = useSelector((state: RootState) => state.export.exportData);

  const availableDataPlugins: DatabaseSettingsDataPluginType[] = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const [selectedDataPlugin, setSelectedDataPlugin] = useState<DatabaseSettingsDataPluginType | undefined>(undefined);
  const [previewTableHeader, setPreviewTableHeader] = useState<string[]>([]);
  const [previewTableData, setPreviewTableData] = useState<JSONObject[]>([]);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set(allItemNames));
  const [previewName, setPreviewName] = useState<string>('');

  // Preview search, pagination & display
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchColumn, setSearchColumn] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(25);
  const [expandColumns, setExpandColumns] = useState<boolean>(false);

  // Reset state when the selected plugin changes, but keep all chips selected
  useEffect(() => {
    dispatch(setExportData(emptyData));
    setSelectedItems(new Set(allItemNames));
    setPreviewName('');
    setPreviewTableHeader([]);
    if (selectedDataPlugin) loadData();
  }, [selectedDataPlugin]);

  // Reset pagination when search or preview changes
  useEffect(() => {
    setCurrentPage(1);
  }, [searchQuery, searchColumn, previewName]);

  // Reset search and column expand when preview changes
  useEffect(() => {
    setSearchQuery('');
    setSearchColumn('');
    setExpandColumns(false);
  }, [previewName]);

  const selectAll = () => setSelectedItems(new Set(allItemNames));
  const deselectAll = () => setSelectedItems(new Set());
  const toggleSelection = (name: string) => {
    setSelectedItems((prev) => {
      const next = new Set(prev);
      if (next.has(name)) {
        next.delete(name);
      } else {
        next.add(name);
      }
      return next;
    });
  };

  async function loadData() {
    dispatch(setExportLoading(true));
    const dP = selectedDataPlugin ? await DataPluginStorage.getDataPlugin(selectedDataPlugin) : undefined;
    if (dP) {
      switch (dP.name) {
        case 'PouchDb':
          dispatch(setExportData(await dP.export!()));
          break;

        case 'Binocular Backend': {
          const xhr = new XMLHttpRequest();
          xhr.open('GET', window.location.protocol + '//' + window.location.hostname + ':48763/api/db-export', true);
          xhr.onload = () => {
            const data = JSON.parse(JSON.stringify(emptyData));
            for (const [key, value] of Object.entries(JSON.parse(xhr.responseText) as ExportDataType)) {
              data[key.replaceAll('_', '-') as keyof ExportDataType] = value;
            }
            dispatch(setExportData(data));
          };
          xhr.send();
          break;
        }
      }
    }
  }

  function setPreviewTable(name: string) {
    if (!exportData[name as keyof ExportDataType]) return;
    setPreviewTableHeader(Object.keys(exportData[name as keyof ExportDataType][0]) ?? []);
    setPreviewName(name);
    setPreviewTableData(exportData[name as keyof ExportDataType]);
  }

  function download(name: string, data: Blob) {
    const url = URL.createObjectURL(data);
    const downloadLink = document.createElement('a');
    downloadLink.href = url;
    downloadLink.download = name;
    downloadLink.click();
  }

  function downloadFile(name: string, data: JSONObject[]) {
    switch (exportDataType) {
      case 'csv':
        download(name + '.csv', new Blob([convertToCSV(data)], { type: 'data:text/csv;charset=utf-8' }));
        break;
      default:
        download(name + '.json', new Blob([JSON.stringify(data)], { type: 'data:text/json;charset=utf-8' }));
        break;
    }
  }

  async function downloadSelected() {
    const data: { [id: string]: JSONObject[] } = {};
    for (const name of Array.from(selectedItems)) {
      data[name] = exportData[name];
    }
    downloadExportCompressed(data, undefined, exportDataType);
  }

  function convertToCSV(jsonObject: JSONObject[]) {
    const items = jsonObject;
    const replacer = (_key: string, value: string) => (value === null ? '' : value);
    const header = Object.keys(items[0]);
    return [header.join(','), ...items.map((row) => header.map((fieldName) => JSON.stringify(row[fieldName], replacer)).join(','))].join(
      '\r\n',
    );
  }

  // Filtered + paginated preview rows
  const filteredRows = previewTableData.filter((row) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    if (searchColumn) {
      return JSON.stringify(row[searchColumn]).toLowerCase().includes(q);
    }
    return Object.values(row).some((v) => JSON.stringify(v).toLowerCase().includes(q));
  });
  const totalPages = Math.max(1, Math.ceil(filteredRows.length / pageSize));
  const pagedRows = filteredRows.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  function renderChip(name: string, count: number, isSelected: boolean) {
    return (
      <div
        key={name}
        className={`badge badge-lg cursor-pointer select-none transition-colors py-3 rounded-full ${isSelected ? 'badge-primary' : 'badge-outline'}`}
        onClick={() => toggleSelection(name)}>
        <span className="mr-1">{name}</span>
        {count > 0 && <span className="text-xs font-mono">({count})</span>}
        <button
          className="btn btn-ghost btn-xs p-0 h-auto min-h-0"
          onClick={(e) => {
            setPreviewTable(name);
            e.stopPropagation();
          }}>
          <ViewIcon />
        </button>
      </div>
    );
  }

  return (
    <dialog id={'exportDialog'} className={'modal'}>
      <div className={`modal-box ${exportType === ExportType.all ? 'max-w-full' : 'w-full'}`}>
        {exportType === ExportType.all && (
          <h3 id={'informationDialogHeadline'} className={'font-bold text-lg underline'}>
            Export
          </h3>
        )}
        {exportType === ExportType.image && (
          <h3 id={'informationDialogHeadline'} className={'font-bold text-lg underline'}>
            Image Export
          </h3>
        )}
        {exportType === ExportType.data && (
          <h3 id={'informationDialogHeadline'} className={'font-bold text-lg underline'}>
            Data Export
          </h3>
        )}

        {exportType === ExportType.image && (
          <div>
            <h3>Preview:</h3>
            <div
              className={'w-full overflow-auto border-base-300 border mb-3'}
              style={{ height: '30rem' }}
              dangerouslySetInnerHTML={{ __html: exportSVGData }}></div>
            <button
              className={'btn btn-primary'}
              onClick={() => {
                download(exportName, new Blob([exportSVGData], { type: 'image/svg+xml;charset=utf-8' }));
              }}>
              Export SVG
            </button>
          </div>
        )}

        {exportType === ExportType.all && (
          <div className={dataExportStyles.chartContainer}>
            <div className={dataExportStyles.mg1}>
              {/* Step 1: Choose Database */}
              <h1>1. Choose Database</h1>
              <div className={'flex flex-wrap gap-3 mb-4'}>
                {availableDataPlugins
                  .filter((dP) => dP.name === 'PouchDb' || dP.name === 'Binocular Backend')
                  .map((dP: DatabaseSettingsDataPluginType) => {
                    const isSelected = selectedDataPlugin?.id === dP.id;
                    return (
                      <div
                        className={`card w-52 bg-base-100 shadow-md border cursor-pointer transition-all relative
                          ${isSelected ? 'border-primary ring-2 ring-primary ring-offset-2' : 'border-base-300 hover:border-primary/50'}`}
                        style={{ background: dP.color }}
                        key={`settingsDatabasePlugin${dP.id}`}
                        onClick={() => {
                          // disable switching while data is loading to not overwhelm the website
                          if (!loading) setSelectedDataPlugin(dP);
                        }}>
                        <div className="card-body py-3 px-4">
                          <div className="flex items-center gap-2">
                            <h2 className="card-title text-sm">
                              {dP.name} #{dP.id}
                            </h2>
                          </div>
                          <div className="flex flex-wrap gap-1">
                            {dP.id === 0 && <div className="badge badge-outline badge-sm">pre-loaded</div>}
                          </div>
                          {dP.parameters.endpoint && (
                            <div className="text-xs">
                              <span className={'font-bold'}>Endpoint: </span>
                              <span>{dP.parameters.endpoint}</span>
                            </div>
                          )}
                          {dP.parameters.fileName && (
                            <div className="text-xs">
                              <span className={'font-bold'}>Database: </span>
                              <span>{dP.parameters.fileName}</span>
                            </div>
                          )}
                        </div>
                        {isSelected && !loading && (
                          <span
                            className="badge badge-sm absolute bottom-2 right-2 border-0 text-white"
                            style={{ background: 'rgba(0,0,0,0.35)' }}>
                            &#10003;
                          </span>
                        )}
                        {isSelected && loading && (
                          <span
                            className="loading loading-spinner loading-lg absolute bottom-2 right-2"
                            style={{ background: 'rgba(0,0,0,0.35)' }}></span>
                        )}
                      </div>
                    );
                  })}
              </div>
              {loading && <p>Be patient, this might take a while</p>}

              {/* Step 2: Choose Export Format */}
              <h1>2. Choose Export Format</h1>
              <div className="flex gap-2 mb-4">
                <button
                  className={`btn btn-sm ${exportDataType === 'json' ? 'btn-primary' : 'btn-outline'}`}
                  onClick={() => dispatch(setExportDataType('json'))}>
                  JSON
                </button>
                <button
                  className={`btn btn-sm ${exportDataType === 'csv' ? 'btn-primary' : 'btn-outline'}`}
                  onClick={() => dispatch(setExportDataType('csv'))}>
                  CSV
                </button>
              </div>

              {/* Step 3: View and Download Data */}
              <h1>3. View and Download Data</h1>
              <div className="flex gap-2 mb-3">
                <button className="btn btn-sm btn-outline" onClick={selectAll}>
                  Select All
                </button>
                <button className="btn btn-sm btn-ghost" onClick={deselectAll}>
                  Deselect All
                </button>
              </div>

              <h2>Collections</h2>
              <div className="flex flex-wrap gap-2 mb-4">
                {Object.keys(exportData)
                  .filter((name) => !name.includes('-'))
                  .map((name) => {
                    const count: number = exportData[name as keyof ExportDataType].length;
                    return renderChip(name, count, selectedItems.has(name));
                  })}
              </div>

              <h2>Relations</h2>
              <div className="flex flex-wrap gap-2 mb-4">
                {Object.keys(exportData)
                  .filter((name) => name.includes('-'))
                  .map((name) => {
                    const count: number = exportData[name as keyof ExportDataType].length;
                    return renderChip(name, count, selectedItems.has(name));
                  })}
              </div>

              {/* Action buttons */}
              <div className="flex gap-3 mb-4 pt-3 border-t border-base-300">
                <button
                  className="btn btn-primary btn-sm"
                  disabled={selectedItems.size === 0 || !selectedDataPlugin}
                  onClick={() => void downloadSelected()}>
                  <DownloadIcon />
                  Download Selected ({selectedItems.size})
                </button>
                <button
                  className="btn btn-outline btn-sm"
                  disabled={!selectedDataPlugin}
                  onClick={() => {
                    if (selectedDataPlugin && !loading) {
                      downloadExportCompressed(exportData, selectedDataPlugin!.metadata, exportDataType);
                    }
                  }}>
                  <DownloadIcon />
                  Download Complete Database
                </button>
              </div>

              {/* Preview table */}
              {previewName && (
                <>
                  {/* Preview header */}
                  <div className="flex items-center justify-between mb-3">
                    <span className="font-semibold text-sm">
                      Preview: <code className="bg-base-200 px-1 rounded">{previewName}</code>
                      {previewTableData.length > 10000 && (
                        <span className="ml-2 text-warning text-xs">(Too many entries — preview may be slow)</span>
                      )}
                    </span>
                    {previewTableData.length > 0 && (
                      <div className="flex gap-2">
                        <button
                          className={`btn btn-sm ${expandColumns ? 'btn-primary' : 'btn-outline'}`}
                          onClick={() => setExpandColumns((v) => !v)}>
                          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="h-4 w-4">
                            <path
                              fillRule="evenodd"
                              d="M2.22 2.22a.75.75 0 0 1 1.06 0L6 4.94V4a.75.75 0 0 1 1.5 0v2.5A1.5 1.5 0 0 1 6 8H3.5a.75.75 0 0 1 0-1.5h.94L2.22 4.28a.75.75 0 0 1 0-1.06Zm11.56 0a.75.75 0 0 1 0 1.06L11.56 5.5h.94a.75.75 0 0 1 0 1.5H10A1.5 1.5 0 0 1 8.5 5.5V3a.75.75 0 0 1 1.5 0v.94l2.22-2.72a.75.75 0 0 1 1.06 0ZM2.22 13.78a.75.75 0 0 1 0-1.06l2.22-2.22H3.5a.75.75 0 0 1 0-1.5H6A1.5 1.5 0 0 1 7.5 10.5V13a.75.75 0 0 1-1.5 0v-.94l-2.72 2.72a.75.75 0 0 1-1.06 0Zm11.56 0a.75.75 0 0 1-1.06 0L10.5 11.56V12.5a.75.75 0 0 1-1.5 0V10a1.5 1.5 0 0 1 1.5-1.5H13a.75.75 0 0 1 0 1.5h-.94l2.72 2.72a.75.75 0 0 1 0 1.06Z"
                              clipRule="evenodd"
                            />
                          </svg>
                          Expand Columns
                        </button>
                        <button className="btn btn-sm btn-outline" onClick={() => downloadFile(previewName, previewTableData)}>
                          <DownloadIcon />
                          Download Preview
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Search bar */}
                  {previewTableData.length > 0 && (
                    <div className="flex gap-2 mb-2">
                      <select
                        className="select select-sm select-bordered"
                        value={searchColumn}
                        onChange={(e) => setSearchColumn(e.target.value)}>
                        <option value="">All columns</option>
                        {previewTableHeader.map((key) => (
                          <option key={key} value={key}>
                            {key}
                          </option>
                        ))}
                      </select>
                      <label className="input input-sm input-bordered flex items-center gap-2 flex-1">
                        <input
                          type="text"
                          className="grow"
                          placeholder="Search…"
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                        />
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="h-4 w-4 opacity-50">
                          <path
                            fillRule="evenodd"
                            d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </label>
                      <span className="text-xs self-center opacity-60 whitespace-nowrap">
                        {filteredRows.length} / {previewTableData.length} rows
                      </span>
                    </div>
                  )}

                  {/* Table */}
                  <div className={dataExportStyles.previewTableContainer}>
                    {previewTableData.length !== 0 ? (
                      <table className="table table-zebra table-sm w-full">
                        <thead>
                          <tr>
                            {previewTableHeader.map((key) => (
                              <th key={key} className="whitespace-nowrap sticky top-0 bg-base-200 z-10">
                                {key}
                              </th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {pagedRows.map((row: JSONObject, i: number) => (
                            <tr key={i}>
                              {previewTableHeader.map((key: string, j: number) => (
                                <td key={j} className={`font-mono text-xs ${expandColumns ? 'whitespace-nowrap' : 'max-w-48 truncate'}`}>
                                  {JSON.stringify(row[key as keyof typeof row])}
                                </td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    ) : (
                      <div className="text-center opacity-50 py-8">No data loaded</div>
                    )}
                  </div>

                  {/* Pagination */}
                  {previewTableData.length > 0 && (
                    <div className="flex items-center justify-between mt-2">
                      <div className="join">
                        <button className="join-item btn btn-sm" disabled={currentPage === 1} onClick={() => setCurrentPage(1)}>
                          «
                        </button>
                        <button className="join-item btn btn-sm" disabled={currentPage === 1} onClick={() => setCurrentPage((p) => p - 1)}>
                          ‹
                        </button>
                        <span className="join-item btn btn-sm btn-disabled pointer-events-none">
                          {currentPage} / {totalPages}
                        </span>
                        <button
                          className="join-item btn btn-sm"
                          disabled={currentPage === totalPages}
                          onClick={() => setCurrentPage((p) => p + 1)}>
                          ›
                        </button>
                        <button
                          className="join-item btn btn-sm"
                          disabled={currentPage === totalPages}
                          onClick={() => setCurrentPage(totalPages)}>
                          »
                        </button>
                      </div>
                      <select
                        className="select select-sm select-bordered"
                        value={pageSize}
                        onChange={(e) => {
                          setPageSize(Number(e.target.value));
                          setCurrentPage(1);
                        }}>
                        {PAGE_SIZE_OPTIONS.map((n) => (
                          <option key={n} value={n}>
                            {n} / page
                          </option>
                        ))}
                      </select>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        )}

        <div className={'modal-action'}>
          <form method={'dialog'}>
            <button className={'btn btn-sm btn-ghost'}>Close</button>
          </form>
        </div>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
}

export default ExportDialog;
