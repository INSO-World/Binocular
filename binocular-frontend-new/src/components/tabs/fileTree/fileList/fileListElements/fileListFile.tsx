import fileListElementsStyles from './fileListElements.module.scss';
import type { FileTreeElementType } from '../../../../../types/data/fileListType.ts';
import FileIcon from '../../../../../assets/file_gray.svg';
import { showFileTreeElementInfo, updateFileListElement } from '../../../../../redux/reducer/data/filesReducer.ts';
import { type AppDispatch, useAppDispatch } from '../../../../../redux';
import { formatName } from '../fileListUtilities/fileTreeUtilities.tsx';
import { type ContextMenuOption, showContextMenu } from '../../../../contextMenu/contextMenuHelper.ts';
import infoIcon from '../../../../../assets/info_gray.svg';
import openInNewIcon from '../../../../../assets/open_in_new_gray.svg';

function FileListFile(props: { file: FileTreeElementType; listOnly?: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();

  function openFileContextMenu(e: React.MouseEvent<HTMLDivElement>) {
    e.preventDefault();
    e.stopPropagation();

    const contextMenuOptions: ContextMenuOption[] = [
      {
        label: 'info',
        icon: infoIcon,
        function: () => dispatch(showFileTreeElementInfo(props.file)),
      },
    ];

    if (props.file.element?.webUrl) {
      contextMenuOptions.push({
        label: 'open in browser',
        icon: openInNewIcon,
        function: () => window.open(props.file.element?.webUrl, '_blank'),
      });
    }

    showContextMenu(e.clientX, e.clientY, contextMenuOptions);
  }

  return (
    <>
      <div className={'flex items-center'}>
        {(props.listOnly === undefined || !props.listOnly) && (
          <input
            type={'checkbox'}
            className={'checkbox checkbox-accent checkbox-xs'}
            checked={props.file.checked}
            onChange={(e) => dispatch(updateFileListElement({ ...props.file, checked: e.target.checked, update: true }))}
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
