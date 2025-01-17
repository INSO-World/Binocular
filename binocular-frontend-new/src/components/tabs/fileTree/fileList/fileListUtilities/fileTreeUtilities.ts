import { FileListElementType, FileListElementTypeType } from '../../../../../types/data/fileListType.ts';
import { DataPluginFile } from '../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';

export function generateFileTree(files: DataPluginFile[]): FileListElementType[] {
  return convertData(files).content;
}

function convertData(files: DataPluginFile[]) {
  const convertedData = { content: [] };
  let id = 0;
  for (const file of files) {
    if (file) {
      const pathParts = file.path.split('/');
      id = genPathObjectString(convertedData.content, pathParts, file, id);
    }
  }
  return convertedData;
}

function genPathObjectString(convertedData: FileListElementType[], pathParts: string[], file: DataPluginFile, id: number) {
  const currElm = pathParts.shift();
  id++;
  if (currElm) {
    if (pathParts.length === 0) {
      convertedData.push({
        name: currElm,
        id: id,
        type: FileListElementTypeType.File,
        checked: true,
        element: file,
        foldedOut: false,
      });
    } else {
      let elem = convertedData.find((d) => d.name === currElm);
      if (elem === undefined) {
        elem = {
          name: currElm,
          id: id,
          type: FileListElementTypeType.Folder,
          children: [],
          checked: true,
          foldedOut: false,
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
