import fileListElementsStyles from './fileListElements.module.scss';
import { FileListElementType, FileListElementTypeType } from '../../../../../types/data/fileListType.ts';
import { useEffect, useState } from 'react';
import FolderIcon from '../../../../../assets/folder_gray.svg';
import FolderOpenIcon from '../../../../../assets/folder_open_gray.svg';
import FileListFile from './fileListFile.tsx';

function FileListFolder(props: { folder: FileListElementType[]; name: string; foldedOut: boolean; checked: boolean }) {
  const [foldedOut, setFoldedOut] = useState(props.foldedOut);
  const [checked, setChecked] = useState(props.checked);
  useEffect(() => {
    setChecked(props.checked);
  }, [props.checked]);
  return (
    <>
      {foldedOut ? (
        <>
          <div className={'flex items-center'}>
            <input
              type={'checkbox'}
              className={'checkbox checkbox-accent checkbox-xs'}
              checked={checked}
              onChange={(e) => setChecked(e.target.checked)}
            />
            <div className={fileListElementsStyles.element} onClick={() => setFoldedOut(false)}>
              <img src={FolderOpenIcon} alt={`folder open ${props.name}`} />
              <span>{props.name}</span>
            </div>
          </div>
          <div className={fileListElementsStyles.inset}>
            {props.folder
              .sort((e) => (e.type === FileListElementTypeType.Folder ? -1 : 1))
              .map((element, i) => {
                if (element.type === FileListElementTypeType.Folder && element.children) {
                  return (
                    <FileListFolder
                      key={`fileListElement${i}`}
                      folder={element.children}
                      name={element.name}
                      foldedOut={false}
                      checked={checked}></FileListFolder>
                  );
                } else {
                  return <FileListFile key={`fileListElement${i}`} file={element} checked={checked}></FileListFile>;
                }
              })}
          </div>
        </>
      ) : (
        <div className={'flex items-center'}>
          <input
            type={'checkbox'}
            className={'checkbox checkbox-accent checkbox-xs'}
            checked={checked}
            onChange={(e) => setChecked(e.target.checked)}
          />
          <div onClick={() => setFoldedOut(true)} className={fileListElementsStyles.element}>
            <img src={FolderIcon} alt={`folder ${props.name}`} />
            <span>{props.name}</span>
          </div>
        </div>
      )}
    </>
  );
}

export default FileListFolder;
