import fileListElementsStyles from './fileListElements.module.scss';
import { FileListElementType } from '../../../../../types/data/fileListType.ts';
import FileIcon from '../../../../../assets/file_gray.svg';
import { useEffect, useState } from 'react';

function FileListFile(props: { file: FileListElementType; checked: boolean }) {
  const [checked, setChecked] = useState(props.checked);
  useEffect(() => {
    setChecked(props.checked);
  }, [props.checked]);
  return (
    <>
      <div className={'flex items-center'}>
        <input
          type={'checkbox'}
          className={'checkbox checkbox-accent checkbox-xs'}
          checked={checked}
          onChange={(e) => setChecked(e.target.checked)}
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
