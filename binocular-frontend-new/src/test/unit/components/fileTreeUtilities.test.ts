import { describe, it, expect, beforeAll, vi } from 'vitest';
import { FileTreeElementTypeType } from '../../../types/data/fileListType';
import type { FileTreeElementType } from '../../../types/data/fileListType';
import type { DataPluginFile } from '../../../plugins/interfaces/dataPluginInterfaces/dataPluginFiles';

// Mock DataPluginStorage to prevent pluginRegistry → PouchDB initialization from hanging in jsdom
vi.mock('../../../utils/dataPluginStorage', () => ({ default: {} }));

// Stub OPFS before module loads
vi.stubGlobal('navigator', {
  storage: {
    getDirectory: vi.fn().mockRejectedValue(new Error('OPFS not available')),
  },
});

let generateFileTree: (files: DataPluginFile[]) => FileTreeElementType[];
let filterFileTree: (fileTree: FileTreeElementType, search: string) => FileTreeElementType;

beforeAll(async () => {
  const mod = await import('../../../components/tabs/fileTree/fileList/fileListUtilities/fileTreeUtilities');
  generateFileTree = mod.generateFileTree;
  filterFileTree = mod.filterFileTree;
});

function makeFile(path: string): DataPluginFile {
  return { path, webUrl: '', maxLength: 0 };
}

describe('generateFileTree', () => {
  it('U41.1 empty files array → empty tree', () => {
    const result = generateFileTree([]);
    expect(result).toEqual([]);
  });

  it('U41.2 single flat file produces one File node', () => {
    const result = generateFileTree([makeFile('index.ts')]);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('index.ts');
    expect(result[0].type).toBe(FileTreeElementTypeType.File);
  });

  it('U41.3 nested path a/b.ts produces folder a containing file b.ts', () => {
    const result = generateFileTree([makeFile('src/index.ts')]);
    expect(result).toHaveLength(1);
    expect(result[0].type).toBe(FileTreeElementTypeType.Folder);
    expect(result[0].name).toBe('src');
    expect(result[0].children).toHaveLength(1);
    expect(result[0].children![0].name).toBe('index.ts');
  });

  it('U41.4 two files in same folder share one folder node', () => {
    const result = generateFileTree([makeFile('src/a.ts'), makeFile('src/b.ts')]);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('src');
    expect(result[0].children).toHaveLength(2);
  });

  it('U41.5 files at different roots produce separate root nodes', () => {
    const result = generateFileTree([makeFile('src/a.ts'), makeFile('lib/b.ts')]);
    expect(result).toHaveLength(2);
    const names = result.map((r) => r.name);
    expect(names).toContain('src');
    expect(names).toContain('lib');
  });
});

describe('filterFileTree', () => {
  function makeTree(): FileTreeElementType {
    return {
      name: '/',
      type: FileTreeElementTypeType.Folder,
      id: 0,
      checked: true,
      foldedOut: true,
      isRoot: true,
      children: [
        {
          name: 'src',
          type: FileTreeElementTypeType.Folder,
          id: 1,
          checked: true,
          foldedOut: false,
          isRoot: false,
          children: [
            {
              name: 'index.ts',
              id: 2,
              type: FileTreeElementTypeType.File,
              checked: true,
              foldedOut: false,
              isRoot: false,
              element: { path: 'src/index.ts', webUrl: '', maxLength: 0 },
            },
            {
              name: 'utils.ts',
              id: 3,
              type: FileTreeElementTypeType.File,
              checked: true,
              foldedOut: false,
              isRoot: false,
              element: { path: 'src/utils.ts', webUrl: '', maxLength: 0 },
            },
          ],
        },
      ],
    };
  }

  it('U41.6 filter matching path returns file', () => {
    const result = filterFileTree(makeTree(), 'index');
    expect(result.children).toHaveLength(1); // src folder
    expect(result.children![0].children).toHaveLength(1); // only index.ts
    expect(result.children![0].children![0].name).toBe('index.ts');
  });

  it('U41.7 filter with no match removes all files, empty folder removed', () => {
    const result = filterFileTree(makeTree(), 'notfound');
    // src folder should be removed (no children match)
    expect(result.children).toHaveLength(0);
  });

  it('U41.8 filter matching all files returns all', () => {
    const result = filterFileTree(makeTree(), '.ts');
    expect(result.children![0].children).toHaveLength(2);
  });

  it('U41.9 leaf node (no children) returns unchanged', () => {
    const leaf: FileTreeElementType = {
      name: 'file.ts',
      id: 1,
      type: FileTreeElementTypeType.File,
      checked: true,
      foldedOut: false,
      isRoot: false,
      element: { path: 'file.ts', webUrl: '', maxLength: 0 },
    };
    const result = filterFileTree(leaf, 'anything');
    expect(result).toBe(leaf); // same reference
  });
});
