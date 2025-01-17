import fileListElementsStyles from './fileListElements.module.scss';
import { FileListElementType } from '../../../../../types/data/fileListType.ts';
import FileIcon from '../../../../../assets/file_gray.svg';
import { updateFileListElement } from '../../../../../redux/reducer/data/filesReducer.ts';
import { AppDispatch, useAppDispatch } from '../../../../../redux';
import { updateFileListElementTypeChecked } from '../fileListUtilities/fileTreeUtilities.ts';

function FileListFile(props: { file: FileListElementType }) {
  const dispatch: AppDispatch = useAppDispatch();
  return (
    <>
      <div className={'flex items-center'}>
        <input
          type={'checkbox'}
          className={'checkbox checkbox-accent checkbox-xs'}
          checked={props.file.element.checked}
          onChange={(e) => dispatch(updateFileListElement(updateFileListElementTypeChecked(props.file, e.target.checked)))}
        />
        <div className={fileListElementsStyles.element}>
          <img src={FileIcon} alt={`folder ${props.file.name}`} />
          <span>{props.file.name}</span>
        </div>
      </div>
    </>
  );
}

export default FileListFile;
