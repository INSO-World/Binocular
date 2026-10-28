import React from 'react';
import { Icon } from '../../../../../../components/icon';
import type { FolderWithRatio, TooltipEntry } from './commitByFileViz.tsx';

type FolderViewProps = {
  folder: FolderWithRatio;
  isVertical: boolean;
  boundsWidth: number;
  boundsHeight: number;
  onNavigate: (folderName: string) => void;
  onHover: (e: React.MouseEvent, main: TooltipEntry, children?: TooltipEntry[]) => void;
  onLeaveHover: () => void;
};

// A folder's own direct children (subfolders, then files), each reduced to a tooltip row — one level deep only.
const childEntries = (folder: FolderWithRatio): TooltipEntry[] => [
  ...Object.entries(folder.subfolders).map(([name, node]) => ({
    label: name,
    additions: node.stats.additions,
    deletions: node.stats.deletions,
    kind: 'folder' as const,
  })),
  ...folder.files.map((file) => ({
    label: file.file.path.split('/').pop() ?? file.file.path,
    additions: file.stats.additions,
    deletions: file.stats.deletions,
    kind: 'file' as const,
  })),
];

export const FolderView: React.FC<FolderViewProps> = ({
  folder,
  isVertical,
  boundsWidth,
  boundsHeight,
  onNavigate,
  onHover,
  onLeaveHover,
}) => {
  return (
    <div
      style={{
        height: `${boundsHeight}px`,
        width: `${boundsWidth}px`,
        display: 'flex',
        flexDirection: isVertical ? 'column' : 'row',
        gap: '4px',
      }}>
      {folder &&
        folder.files &&
        folder.files.map((file) => {
          const ratio = file.changeRatio / folder.changeRatio;
          const style = isVertical ? { height: `${ratio * 100}%`, width: '100%' } : { width: `${ratio * 100}%`, height: '100%' };
          const fileLabel = file.file.path.split('/').pop() ?? file.file.path;
          return (
            <div
              key={file.file.path}
              style={{
                ...style,
                backgroundColor: getFileColour(file.stats.additions, file.stats.deletions),
                display: 'flex',
                cursor: 'default',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '0.7rem',
                borderRadius: '10px',
              }}
              onMouseEnter={(e) =>
                onHover(e, { label: fileLabel, additions: file.stats.additions, deletions: file.stats.deletions, kind: 'file' })
              }
              onMouseMove={(e) =>
                onHover(e, { label: fileLabel, additions: file.stats.additions, deletions: file.stats.deletions, kind: 'file' })
              }
              onMouseLeave={onLeaveHover}>
              <Icon name="file" size="w-4 h-4" colorClass="inherit" className="mr-1 shrink-0" />
              {fileLabel}
            </div>
          );
        })}

      {folder &&
        Object.entries(folder.subfolders).map(([childName, childNode]) => {
          const ratio = childNode.changeRatio / folder.changeRatio;
          const style = isVertical ? { height: `${ratio * 100}%`, width: '100%' } : { width: `${ratio * 100}%`, height: '100%' };

          return (
            <div
              key={childName}
              style={{
                ...style,
                backgroundColor: getFileColour(childNode.stats.additions, childNode.stats.deletions),
                border: '2px solid var(--color-primary)',
                borderRadius: '10px',
                padding: '4px',
                position: 'relative',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '0.8rem',
                fontWeight: 'bold',
              }}
              onClick={() => onNavigate(childName)}
              onMouseEnter={(e) =>
                onHover(
                  e,
                  { label: childName, additions: childNode.stats.additions, deletions: childNode.stats.deletions, kind: 'folder' },
                  childEntries(childNode),
                )
              }
              onMouseMove={(e) =>
                onHover(
                  e,
                  { label: childName, additions: childNode.stats.additions, deletions: childNode.stats.deletions, kind: 'folder' },
                  childEntries(childNode),
                )
              }
              onMouseLeave={onLeaveHover}>
              <Icon name="folder" size="w-5 h-5" colorClass="primary" className="mr-1 shrink-0" />
              {childName}
            </div>
          );
        })}
    </div>
  );
};

const getFileColour = (add: number, del: number) => {
  const total = add + del;
  const ratio = add / total;
  const vividness = Math.abs(0.5 - ratio) * 2;
  const alpha = 0.75;
  if (add === del) {
    return `rgb(230, 230, 230, ${alpha})`;
  } else if (add < del) {
    return `rgb(${230 + vividness * 25}, ${255 - vividness * 255}, ${255 - vividness * 255}, ${alpha})`;
  } else {
    return `rgb(${255 - vividness * 255}, ${230 + vividness * 25}, ${255 - vividness * 255}, ${alpha})`;
  }
};
