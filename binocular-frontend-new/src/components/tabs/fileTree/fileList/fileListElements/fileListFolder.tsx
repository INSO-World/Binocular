import fileListElementsStyles from './fileListElements.module.scss';
import { FileTreeElementType, FileTreeElementTypeType } from '../../../../../types/data/fileListType.ts';
import FolderIcon from '../../../../../assets/folder_gray.svg';
import FolderOpenIcon from '../../../../../assets/folder_open_gray.svg';
import FileListFile from './fileListFile.tsx';
import { AppDispatch, useAppDispatch } from '../../../../../redux';
import { updateFileListElement } from '../../../../../redux/reducer/data/filesReducer.ts';
import { formatName } from '../fileListUtilities/fileTreeUtilities.tsx';

function FileListFolder(props: { folder: FileTreeElementType; foldedOut: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();

  return (
    <>
      {props.folder.foldedOut ? (
        <>
          <div className={'flex items-center'}>
            {props.folder.id !== undefined && (
              <input
                type={'checkbox'}
                className={'checkbox checkbox-accent checkbox-xs'}
                checked={props.folder.checked}
                onChange={(e) => dispatch(updateFileListElement({ ...props.folder, checked: e.target.checked }))}
              />
            )}
            <div
              className={fileListElementsStyles.element}
              onClick={() => dispatch(updateFileListElement({ ...props.folder, foldedOut: false }))}>
              <img src={FolderOpenIcon} alt={`folder open ${props.folder.name}`} />
              <span>{formatName(props.folder.searchTerm, props.folder.name)}</span>
            </div>
          </div>
          <div className={fileListElementsStyles.inset}>
            {props.folder.children &&
              props.folder.children
                .slice()
                .sort(function (e1, e2) {
                  if (e1.name < e2.name) {
                    return 1;
                  }
                  if (e1.name > e2.name) {
                    return -1;
                  }
                  return 0;
                })
                .sort((e) => (e.type === FileTreeElementTypeType.Folder ? -1 : 1))
                .map((element, i) => {
                  if (element.type === FileTreeElementTypeType.Folder && element.children) {
                    return <FileListFolder key={`fileListElement${i}`} folder={element} foldedOut={false}></FileListFolder>;
                  } else {
                    return <FileListFile key={`fileListElement${i}`} file={element}></FileListFile>;
                  }
                })}
          </div>
        </>
      ) : (
        <div className={'flex items-center'}>
          <input
            type={'checkbox'}
            className={'checkbox checkbox-accent checkbox-xs'}
            checked={props.folder.checked}
            onChange={(e) => dispatch(updateFileListElement({ ...props.folder, checked: e.target.checked }))}
          />
          <div
            onClick={() => dispatch(updateFileListElement({ ...props.folder, foldedOut: true }))}
            className={fileListElementsStyles.element}>
            <img src={FolderIcon} alt={`folder ${props.folder.name}`} />
            <span>{formatName(props.folder.searchTerm, props.folder.name)}</span>
          </div>
        </div>
      )}
    </>
  );
}

export default FileListFolder;
