import fileListElementsStyles from './fileListElements.module.scss';
import { FileTreeElementType } from '../../../../../types/data/fileListType.ts';
import FileIcon from '../../../../../assets/file_gray.svg';
import { updateFileListElement } from '../../../../../redux/reducer/data/filesReducer.ts';
import { AppDispatch, useAppDispatch } from '../../../../../redux';
import { formatName } from '../fileListUtilities/fileTreeUtilities.tsx';

function FileListFile(props: { file: FileTreeElementType }) {
  const dispatch: AppDispatch = useAppDispatch();
  return (
    <>
      <div className={'flex items-center'}>
        <input
          type={'checkbox'}
          className={'checkbox checkbox-accent checkbox-xs'}
          checked={props.file.checked}
          onChange={(e) => dispatch(updateFileListElement({ ...props.file, checked: e.target.checked }))}
        />
        <div className={fileListElementsStyles.element}>
          <img src={FileIcon} alt={`folder ${props.file.name}`} />
          <span>{formatName(props.file.searchTerm, props.file.name)}</span>
        </div>
      </div>
    </>
  );
}

export default FileListFile;
