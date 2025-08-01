import { useSelector } from 'react-redux';
import { RootState } from '../../../../redux';
import { FileTreeElementType, FileTreeElementTypeType } from '../../../../types/data/fileListType.ts';
import FileListFolder from '../fileList/fileListElements/fileListFolder.tsx';
import { filterFileTree } from '../fileList/fileListUtilities/fileTreeUtilities.tsx';
import FileSearch from '../fileSearch/fileSearch.tsx';
import { useState } from 'react';
import FileTreeElementInfoDialogStyled from './fileTreeElementInfoDialog.module.scss';

function FileTreeElementInfoDialog() {
  const selectedFileTreeElement: FileTreeElementType | undefined = useSelector((state: RootState) => state.files.selectedFileTreeElement);
  const [fileSearch, setFileSearch] = useState('');

  return (
    <dialog id={'fileTreeElementInfoDialog'} className={'modal'}>
      <div className={'modal-box'}>
        {selectedFileTreeElement && (
          <>
            <h3 id={'informationDialogHeadline'} className={'font-bold text-lg mb-2 underline'}>
              {selectedFileTreeElement.name}
            </h3>
            <h2>Type</h2>
            <div>{selectedFileTreeElement.type}</div>

            <h2>File Tree State</h2>
            <div>
              <span className={'mr-2'}>
                {selectedFileTreeElement.foldedOut ? (
                  <div className="badge badge-accent">folded out</div>
                ) : (
                  <div className="badge badge-accent badge-outline">folded in</div>
                )}
              </span>
              <span className={'mr-2'}>
                {selectedFileTreeElement.checked ? (
                  <div className="badge badge-accent">checked</div>
                ) : (
                  <div className="badge badge-accent badge-outline">unchecked</div>
                )}
              </span>
            </div>

            {selectedFileTreeElement.type === FileTreeElementTypeType.File && selectedFileTreeElement.element && (
              <>
                <h2>Path</h2>
                <div>{selectedFileTreeElement.element.path}</div>
                <h2>Max Length</h2>
                <div>{selectedFileTreeElement.element.maxLength}</div>
                <h2>Url</h2>
                <div>
                  <a href={selectedFileTreeElement.element.webUrl} target="_blank" rel="noreferrer">
                    {selectedFileTreeElement.element.webUrl}
                  </a>
                </div>
              </>
            )}

            {selectedFileTreeElement.type === FileTreeElementTypeType.Folder && (
              <>
                <h2>Folder Content</h2>
                <FileSearch setFileSearch={setFileSearch}></FileSearch>
                <div className={FileTreeElementInfoDialogStyled.FolderContentContainer}>
                  <FileListFolder
                    folder={filterFileTree(selectedFileTreeElement, fileSearch)}
                    foldedOut={true}
                    listOnly={true}></FileListFolder>
                </div>
              </>
            )}
          </>
        )}

        <div className={'modal-action'}>
          <form method={'dialog'}>
            <button className={'btn btn-sm btn-accent'}>Close</button>
          </form>
        </div>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
}

export default FileTreeElementInfoDialog;
