import fileListElementsStyles from './fileListElements.module.scss';
import { FileTreeElementType } from '../../../../../types/data/fileListType.ts';
import FileIcon from '../../../../../assets/file_gray.svg';
import { showFileTreeElementInfo, updateFileListElement } from '../../../../../redux/reducer/data/filesReducer.ts';
import { AppDispatch, useAppDispatch } from '../../../../../redux';
import { formatName } from '../fileListUtilities/fileTreeUtilities.tsx';
import { showContextMenu } from '../../../../contextMenu/contextMenuHelper.ts';
import infoIcon from '../../../../../assets/info_gray.svg';

function FileListFile(props: { file: FileTreeElementType; listOnly?: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();

  function openFileContextMenu(e: React.MouseEvent<HTMLDivElement>) {
    e.preventDefault();
    e.stopPropagation();
    showContextMenu(e.clientX, e.clientY, [
      {
        label: 'info',
        icon: infoIcon,
        function: () => dispatch(showFileTreeElementInfo(props.file)),
      },
    ]);
  }

  return (
    <>
      <div className={'flex items-center'}>
        {(props.listOnly === undefined || !props.listOnly) && (
          <input
            type={'checkbox'}
            className={'checkbox checkbox-accent checkbox-xs'}
            checked={props.file.checked}
            onChange={(e) => dispatch(updateFileListElement({ ...props.file, checked: e.target.checked }))}
          />
        )}
        <div
          className={fileListElementsStyles.element}
          onClick={() => {
            if (props.listOnly === true) {
              dispatch(showFileTreeElementInfo(props.file));
            }
          }}
          onContextMenu={(e) => {
            openFileContextMenu(e);
          }}>
          <img src={FileIcon} alt={`folder ${props.file.name}`} />
          <span>{formatName(props.file.searchTerm, props.file.name)}</span>
        </div>
      </div>
    </>
  );
}

export default FileListFile;
