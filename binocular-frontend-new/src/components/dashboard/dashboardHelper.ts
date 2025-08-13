import type { DashboardItemDTO, DashboardItemType } from '../../types/general/dashboardItemType.ts';
import type { MutableRefObject, RefObject } from 'react';
import dashboardStyles from './dashboard.module.scss';
import { DragResizeMode } from './resizeMode.ts';

export function moveResizeDashboardItem(dashboardItem: DashboardItemType, rowCount: number, gridMultiplier: number, columnCount: number) {
  const dashboardItemElm = document.getElementById(`dashboardItem${dashboardItem.id}`);
  const dashboardItemSettingsElm = document.getElementById(`dashboardItem${dashboardItem.id}_settings`)?.children[0] as HTMLElement;
  const dashboardItemHelpElm = document.getElementById(`dashboardItem${dashboardItem.id}_help`)?.children[0] as HTMLElement;
  if (dashboardItemElm) {
    if (dashboardItem.y !== undefined) {
      dashboardItemElm.style.top = `calc(${(100.0 / (rowCount * gridMultiplier)) * dashboardItem.y}% + 10px)`;
    }
    if (dashboardItem.x !== undefined) {
      dashboardItemElm.style.left = `calc(${(100.0 / (columnCount * gridMultiplier)) * dashboardItem.x}% + 10px)`;
    }
    dashboardItemElm.style.height = `calc(${(100.0 / (rowCount * gridMultiplier)) * dashboardItem.height}% - 20px)`;
    dashboardItemElm.style.width = `calc(${(100.0 / (columnCount * gridMultiplier)) * dashboardItem.width}% - 20px)`;
  }
  if (dashboardItemSettingsElm) {
    if (dashboardItem.y !== undefined) {
      dashboardItemSettingsElm.style.top = `calc(${(100.0 / (rowCount * gridMultiplier)) * dashboardItem.y}% + 10px + 1.5rem)`;
    }
    if (dashboardItem.x !== undefined) {
      dashboardItemSettingsElm.style.left = `calc(${(100.0 / (columnCount * gridMultiplier)) * (dashboardItem.x + dashboardItem.width)}% - 10px - 20rem)`;
    }
  }
  if (dashboardItemHelpElm) {
    if (dashboardItem.y !== undefined) {
      dashboardItemHelpElm.style.top = `calc(${(100.0 / (rowCount * gridMultiplier)) * dashboardItem.y}% + 10px + 1.5rem)`;
    }
    if (dashboardItem.x !== undefined) {
      dashboardItemHelpElm.style.left = `calc(${(100.0 / (columnCount * gridMultiplier)) * (dashboardItem.x + dashboardItem.width)}% - 10px - 20rem)`;
    }
  }
}

export function clearHighlightDropArea(dragIndicatorRef: RefObject<HTMLDivElement | null>, columnCount: number, rowCount: number) {
  if (dragIndicatorRef.current !== null) {
    dragIndicatorRef.current.style.display = 'none';
  }
  for (let y = 0; y < rowCount; y++) {
    for (let x = 0; x < columnCount; x++) {
      document.getElementById('highlightY' + y + 'X' + x)?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightActive);
      document.getElementById('highlightY' + y + 'X' + x)?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightNotPossible);
    }
  }
}

export function highlightDropArea(
  movingItem: MutableRefObject<DashboardItemDTO>,
  dashboardState: number[][],
  rowCount: number,
  columnCount: number,
  gridMultiplier: number,
  posX: number,
  posY: number,
  width: number,
  height: number,
): boolean {
  let placeable = true;
  for (let y = 0; y < rowCount; y++) {
    for (let x = 0; x < columnCount; x++) {
      if (y > posY - 1 && x > posX - 1 && y < posY + height && x < posX + width) {
        if (
          dashboardState[y * gridMultiplier][x * gridMultiplier] !== 0 &&
          dashboardState[y * gridMultiplier][x * gridMultiplier] !== movingItem.current.id
        ) {
          document.getElementById('highlightY' + y + 'X' + x)?.classList.add(dashboardStyles.dashboardBackgroundCellHighlightNotPossible);
          placeable = false;
        } else {
          document.getElementById('highlightY' + y + 'X' + x)?.classList.add(dashboardStyles.dashboardBackgroundCellHighlightActive);
        }
      } else {
        document.getElementById('highlightY' + y + 'X' + x)?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightActive);
        document.getElementById('highlightY' + y + 'X' + x)?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightNotPossible);
      }
    }
  }
  return placeable;
}

export function setDragResizeMode(
  dragResizeZoneRef: RefObject<HTMLDivElement | null>,
  dragResizeMode: MutableRefObject<DragResizeMode>,
  newDragResizeMode: DragResizeMode,
) {
  dragResizeMode.current = newDragResizeMode;
  if (dragResizeZoneRef.current) {
    if (dragResizeMode.current !== DragResizeMode.none) {
      dragResizeZoneRef.current.style.display = 'block';
    } else {
      dragResizeZoneRef.current.style.display = 'none';
    }
  }
}

export function placeDragIndicator(
  dragIndicatorRef: RefObject<HTMLDivElement | null>,
  movingItem: MutableRefObject<DashboardItemDTO>,
  columnCount: number,
  gridMultiplier: number,
  rowCount: number,
) {
  if (dragIndicatorRef.current !== null && movingItem.current.x !== undefined && movingItem.current.y !== undefined) {
    dragIndicatorRef.current.style.display = 'block';
    dragIndicatorRef.current.style.top = `calc(${(100.0 / rowCount / gridMultiplier) * movingItem.current.y}% + 10px)`;
    dragIndicatorRef.current.style.left = `calc(${(100.0 / columnCount / gridMultiplier) * movingItem.current.x}% + 10px)`;
    dragIndicatorRef.current.style.width = `calc(${(100.0 / columnCount / gridMultiplier) * movingItem.current.width}% - 20px)`;
    dragIndicatorRef.current.style.height = `calc(${(100.0 / rowCount / gridMultiplier) * movingItem.current.height}% - 20px)`;
  }
}

export function moveDragIndicator(
  dragIndicatorRef: RefObject<HTMLDivElement | null>,
  movingItem: MutableRefObject<DashboardItemDTO>,
  dragResizeMode: MutableRefObject<DragResizeMode>,
  movement: { movementX: number; movementY: number },
  targetX: number,
  cellSize: number,
  targetY: number,
  targetWidth: number,
  gridMultiplier: number,
  targetHeight: number,
  placeableItem: DashboardItemType,
  dashboardState: number[][],
  rowCount: number,
  columnCount: number,
) {
  if (dragIndicatorRef.current !== null && movingItem.current.x !== undefined && movingItem.current.y !== undefined) {
    switch (dragResizeMode.current) {
      case DragResizeMode.drag:
        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + movement.movementX + 'px';
        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + movement.movementY + 'px';
        targetX = Math.round((dragIndicatorRef.current.offsetLeft + movement.movementX) / cellSize);
        targetY = Math.round((dragIndicatorRef.current.offsetTop + movement.movementY) / cellSize);
        targetWidth = movingItem.current.width / gridMultiplier;
        targetHeight = movingItem.current.height / gridMultiplier;
        break;
      case DragResizeMode.resizeTop:
        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + movement.movementY + 'px';
        dragIndicatorRef.current.style.height = dragIndicatorRef.current.offsetHeight - movement.movementY + 'px';
        targetX = movingItem.current.x / gridMultiplier;
        targetY = Math.round((dragIndicatorRef.current.offsetTop + movement.movementY) / cellSize);
        targetWidth = movingItem.current.width / gridMultiplier;
        targetHeight = Math.round((dragIndicatorRef.current.offsetHeight + movement.movementY) / cellSize);
        break;
      case DragResizeMode.resizeRight:
        dragIndicatorRef.current.style.width = dragIndicatorRef.current.offsetWidth + movement.movementX + 'px';
        targetX = movingItem.current.x / gridMultiplier;
        targetY = movingItem.current.y / gridMultiplier;
        targetWidth = Math.round((dragIndicatorRef.current.offsetWidth + movement.movementX) / cellSize);
        targetHeight = movingItem.current.height / gridMultiplier;
        break;
      case DragResizeMode.resizeBottom:
        dragIndicatorRef.current.style.height = dragIndicatorRef.current.offsetHeight + movement.movementY + 'px';
        targetX = movingItem.current.x / gridMultiplier;
        targetY = movingItem.current.y / gridMultiplier;
        targetWidth = movingItem.current.width / gridMultiplier;
        targetHeight = Math.round((dragIndicatorRef.current.offsetHeight + movement.movementX) / cellSize);
        break;
      case DragResizeMode.resizeLeft:
        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + movement.movementX + 'px';
        dragIndicatorRef.current.style.width = dragIndicatorRef.current.offsetWidth - movement.movementX + 'px';
        targetX = Math.round((dragIndicatorRef.current.offsetLeft + movement.movementX) / cellSize);
        targetY = movingItem.current.y / gridMultiplier;
        targetWidth = Math.round((dragIndicatorRef.current.offsetWidth + movement.movementX) / cellSize);
        targetHeight = movingItem.current.height / gridMultiplier;
        break;
      case DragResizeMode.place:
        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + movement.movementX + 'px';
        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + movement.movementY + 'px';
        targetX = Math.round((dragIndicatorRef.current.offsetLeft + movement.movementX) / cellSize);
        targetY = Math.round((dragIndicatorRef.current.offsetTop + movement.movementY) / cellSize);
        targetWidth = placeableItem.width / gridMultiplier;
        targetHeight = placeableItem.height / gridMultiplier;
        break;
      default:
        break;
    }

    highlightDropArea(movingItem, dashboardState, rowCount, columnCount, gridMultiplier, targetX, targetY, targetWidth, targetHeight);
  }
  return { targetX, targetY, targetWidth, targetHeight };
}
