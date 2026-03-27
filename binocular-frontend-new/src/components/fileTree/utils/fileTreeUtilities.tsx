import fileListElementsStyles from '../fileTreeElements/fileTreeElements.module.scss';
import type { JSX } from 'react';
import { type FileTreeElementType, FileTreeElementTypeType } from '../../../types/data/fileListType';
import type { DataPluginFile } from '../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles';

export function generateFileTree(files: DataPluginFile[]): FileTreeElementType[] {
  return convertData(files).content;
}

function convertData(files: DataPluginFile[]) {
  const convertedData: { content: FileTreeElementType[] } = { content: [] };
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
  let formattedName = [<span key={'formattedNamePart0'}>{name}</span>];
  if (searchTerm) {
    const searchParts: string[] = searchTerm ? searchTerm.split('/') : [];
    for (const searchPart of searchParts) {
      if (name.toLowerCase().includes(searchPart.toLowerCase())) {
        const nameParts = splitAtFirst(name, searchPart).map((part, i) => <span key={`formatedNamePart${i}`}>{part}</span>);
        formattedName = [
          nameParts[0],
          <span key={'formattedNamePartMatch'} className={fileListElementsStyles.searchMark}>
            {searchPart}
          </span>,
          nameParts[1],
        ];
        break;
      }
    }
  }
  return formattedName;
}

function splitAtFirst(str: string, delimiter: string): [string, string] {
  const index = str.indexOf(delimiter);
  if (index === -1) return [str, ''];
  return [str.slice(0, index), str.slice(index + delimiter.length)];
}

export function updateFileTreeRecursive(fileTree: FileTreeElementType, element: FileTreeElementType, checked?: boolean): string[] {
  const updatedPaths: string[] = [];
  if (fileTree.children) {
    fileTree.children = fileTree.children.map((f: FileTreeElementType) => {
      let elementChecked = checked;
      if (f.id === element.id) {
        if (f.element?.path && !updatedPaths.includes(f.element.path)) {
          updatedPaths.push(f.element.path);
        }
        elementChecked = element.checked;
        f.foldedOut = element.foldedOut;
      }
      if (elementChecked !== undefined) {
        if (f.element?.path && !updatedPaths.includes(f.element.path)) {
          updatedPaths.push(f.element.path);
        }
        f.checked = elementChecked;
      }
      updatedPaths.push(...updateFileTreeRecursive(f, element, elementChecked));
      return f;
    });
  }
  return updatedPaths;
}

export function invertFileTreeSelection(fileTree: FileTreeElementType) {
  if (fileTree.children) {
    fileTree.children.map((f: FileTreeElementType) => {
      f.checked = !f.checked;
      invertFileTreeSelection(f);
    });
  }
}
