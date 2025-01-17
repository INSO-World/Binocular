import { FileListElementType, FileListElementTypeType } from '../../../../../types/data/fileListType.ts';
import { FileType } from '../../../../../types/data/fileType.ts';

export function generateFileTree(files: FileType[]): FileListElementType[] {
  return convertData(files).content;
}

function convertData(files: FileType[]) {
  const convertedData = { content: [] };
  let id = 0;
  for (const file of files) {
    if (file.file) {
      const pathParts = file.file.path.split('/');
      id = genPathObjectString(convertedData.content, pathParts, file, id);
    }
  }
  return convertedData;
}

function genPathObjectString(convertedData: FileListElementType[], pathParts: string[], file: FileType, id: number) {
  const currElm = pathParts.shift();
  id++;
  if (currElm) {
    if (pathParts.length === 0) {
      convertedData.push({
        name: currElm,
        id: id,
        type: FileListElementTypeType.File,
        element: file,
      });
    } else {
      let elem = convertedData.find((d) => d.name === currElm);
      if (elem === undefined) {
        elem = {
          name: currElm,
          id: id,
          type: FileListElementTypeType.Folder,
          children: [],
          element: { checked: true },
        };
        if (elem.children) {
          id = genPathObjectString(elem.children, pathParts, file, id);
          convertedData.push(elem);
        }
      } else {
        if (elem.children) {
          id = genPathObjectString(elem.children, pathParts, file, id);
        }
      }
    }
  }
  return id;
}

export function updateFileListElementTypeChecked(fileListElement: FileListElementType, checked: boolean) {
  return { ...fileListElement, element: { ...fileListElement.element, checked: checked } };
}
