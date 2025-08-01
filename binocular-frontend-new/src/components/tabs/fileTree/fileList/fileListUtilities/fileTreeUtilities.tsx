import { FileTreeElementType, FileTreeElementTypeType } from '../../../../../types/data/fileListType.ts';
import { DataPluginFile } from '../../../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles.ts';
import fileListElementsStyles from '../fileListElements/fileListElements.module.scss';

export function generateFileTree(files: DataPluginFile[]): FileTreeElementType[] {
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

function genPathObjectString(convertedData: FileTreeElementType[], pathParts: string[], file: DataPluginFile, id: number) {
  const currElm = pathParts.shift();
  id++;
  if (currElm) {
    if (pathParts.length === 0) {
      convertedData.push({
        name: currElm,
        id: id,
        type: FileTreeElementTypeType.File,
        checked: true,
        element: file,
        foldedOut: false,
        isRoot: false,
      });
    } else {
      let elem = convertedData.find((d) => d.name === currElm);
      if (elem === undefined) {
        elem = {
          name: currElm,
          id: id,
          type: FileTreeElementTypeType.Folder,
          children: [],
          checked: true,
          foldedOut: false,
          isRoot: false,
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

export function filterFileTree(fileTree: FileTreeElementType, search: string): FileTreeElementType {
  if (fileTree.children) {
    return {
      ...fileTree,
      searchTerm: search,
      children: fileTree.children
        ?.map((child) => {
          if (child.type === FileTreeElementTypeType.Folder) {
            return filterFileTree(child, search);
          } else {
            return { ...child, searchTerm: search };
          }
        })
        .filter((child) => {
          if (child.type === FileTreeElementTypeType.Folder && child.children) {
            return child.children.length > 0;
          }
          return child.element?.path.toLowerCase().includes(search.toLowerCase());
        }),
    };
  } else {
    return fileTree;
  }
}

export function formatName(searchTerm: string | undefined, name: string): JSX.Element[] {
  let formatedName = [<span key={'formatedNamePart0'}>{name}</span>];
  if (searchTerm) {
    const searchParts: string[] = searchTerm ? searchTerm.split('/') : [];
    for (const searchPart of searchParts) {
      if (name.toLowerCase().includes(searchPart.toLowerCase())) {
        const nameParts = name.split(new RegExp(searchPart, 'i')).map((part, i) => <span key={`formatedNamePart${i}`}>{part}</span>);
        formatedName = [
          nameParts[0],
          <span key={'formatedNamePartMatch'} className={fileListElementsStyles.searchMark}>
            {searchPart}
          </span>,
          nameParts[1],
        ];
        break;
      }
    }
  }
  return formatedName;
}
