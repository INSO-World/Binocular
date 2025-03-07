import dashboardStyles from './dashboard.module.scss';
import { createRef, useEffect, useState } from 'react';
import DashboardItem from './dashboardItem/dashboardItem.tsx';
import { DragResizeMode } from './resizeMode.ts';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, useAppDispatch } from '../../redux';
import {
  addDashboardItem,
  moveDashboardItem,
  setDragResizeMode,
  deleteDashboardItem,
} from '../../redux/reducer/general/dashboardReducer.ts';
import { SettingsGeneralGridSize } from '../../types/settings/generalSettingsType.ts';
import { DashboardItemDTO, DashboardItemType } from '../../types/general/dashboardItemType.ts';
import { DatabaseSettingsDataPluginType } from '../../types/settings/databaseSettingsType.ts';
import { addNotification } from '../../redux/reducer/general/notificationsReducer.ts';
import { AlertType } from '../../types/general/alertType.ts';

function Dashboard() {
  const dispatch: AppDispatch = useAppDispatch();

  const [itemMoved, setItemMoved] = useState(false);

  const gridSize = useSelector((state: RootState) => state.settings.general.gridSize);
  let cellCount: number;
  let gridMultiplier: number;

  switch (gridSize) {
    case SettingsGeneralGridSize.small:
      cellCount = 40;
      gridMultiplier = 1;
      break;
    case SettingsGeneralGridSize.medium:
    default:
      cellCount = 20;
      gridMultiplier = 2;
      break;
    case SettingsGeneralGridSize.large:
      cellCount = 10;
      gridMultiplier = 4;
      break;
  }

  const columnCount = cellCount;
  const rowCount = cellCount;

  const dashboardRef = createRef<HTMLDivElement>();
  const dragIndicatorRef = createRef<HTMLDivElement>();
  const [cellSize, setCellSize] = useState(0);
  const [movingItem, setMovingItem] = useState<DashboardItemDTO>({ id: 0, x: 0, y: 0, width: 0, height: 0 });

  let targetX = 0;
  let targetY = 0;
  let targetWidth = 0;
  let targetHeight = 0;

  const dashboardItems = useSelector((state: RootState) => state.dashboard.dashboardItems);
  const dragResizeMode = useSelector((state: RootState) => state.dashboard.dragResizeMode);
  const placeableItem = useSelector((state: RootState) => state.dashboard.placeableItem);
  const dashboardState = useSelector((state: RootState) => state.dashboard.dashboardState);

  const configuredDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  const defaultDataPlugin = configuredDataPlugins.filter((dP: DatabaseSettingsDataPluginType) => dP.isDefault)[0];

  function setDragResizeItem(item: DashboardItemDTO, mode: DragResizeMode) {
    setMovingItem(item);
    dispatch(setDragResizeMode(mode));
  }

  function clearHighlightDropArea(columnCount: number, rowCount: number) {
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

  function highlightDropArea(posX: number, posY: number, width: number, height: number): boolean {
    let placeable = true;
    for (let y = 0; y < rowCount; y++) {
      for (let x = 0; x < columnCount; x++) {
        if (y > posY - 1 && x > posX - 1 && y < posY + height && x < posX + width) {
          if (
            dashboardState[y * gridMultiplier][x * gridMultiplier] !== 0 &&
            dashboardState[y * gridMultiplier][x * gridMultiplier] !== movingItem.id
          ) {
            document.getElementById('highlightY' + y + 'X' + x)?.classList.add(dashboardStyles.dashboardBackgroundCellHighlightNotPossible);
            placeable = false;
          } else {
            document.getElementById('highlightY' + y + 'X' + x)?.classList.add(dashboardStyles.dashboardBackgroundCellHighlightActive);
          }
        } else {
          document.getElementById('highlightY' + y + 'X' + x)?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightActive);
          document
            .getElementById('highlightY' + y + 'X' + x)
            ?.classList.remove(dashboardStyles.dashboardBackgroundCellHighlightNotPossible);
        }
      }
    }
    return placeable;
  }

  useEffect(() => {
    if (dashboardRef.current !== null) {
      setCellSize(dashboardRef.current.offsetWidth / columnCount);
    }
  }, [columnCount, dashboardRef, gridSize]);

  return (
    <>
      <div className={dashboardStyles.dashboard} ref={dashboardRef}>
        <div className={dashboardStyles.dashboardContent}>
          <table className={dashboardStyles.dashboardBackground}>
            <tbody>
              {[...Array(rowCount).keys()].map((row) => {
                return (
                  <tr key={'dashboardBackgroundRow' + row}>
                    {[...Array(columnCount).keys()].map((col) => {
                      return (
                        <td
                          key={'dashboardBackgroundCol' + col}
                          style={{
                            width: cellSize,
                            height: cellSize,
                          }}>
                          <div id={'cellY' + row + 'X' + col} className={dashboardStyles.dashboardBackgroundCell}>
                            <div id={'highlightY' + row + 'X' + col} className={dashboardStyles.dashboardBackgroundCellHighlight}></div>
                          </div>
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
          <>
            {dashboardItems.map((dashboardItem: DashboardItemType) => {
              return (
                <DashboardItem
                  key={'dashboardItem' + dashboardItem.id}
                  cellSize={cellSize}
                  item={dashboardItem}
                  colCount={columnCount * gridMultiplier}
                  rowCount={rowCount * gridMultiplier}
                  setDragResizeItem={setDragResizeItem}
                  deleteItem={(item) => dispatch(deleteDashboardItem(item))}></DashboardItem>
              );
            })}
          </>
          <div className={dashboardStyles.dragIndicator} ref={dragIndicatorRef} id={'dragIndicator'}></div>
          {dragResizeMode !== DragResizeMode.none && (
            <>
              <div
                className={dashboardStyles.dragResizeZone}
                onMouseEnter={(event) => {
                  setItemMoved(true);
                  event.stopPropagation();
                  if (dragIndicatorRef.current !== null && movingItem.x !== undefined && movingItem.y !== undefined) {
                    if (dragResizeMode === DragResizeMode.place) {
                      dragIndicatorRef.current.style.display = 'block';
                      dragIndicatorRef.current.style.top = `calc(${(50.0 / columnCount / gridMultiplier) * (1 - placeableItem.width)}% + 10px)`;
                      dragIndicatorRef.current.style.left = `calc(${(50.0 / columnCount / gridMultiplier) * (1 - placeableItem.height)}% + 10px)`;
                      dragIndicatorRef.current.style.width = `calc(${(100.0 / columnCount / gridMultiplier) * placeableItem.width}% - 20px)`;
                      dragIndicatorRef.current.style.height = `calc(${(100.0 / rowCount / gridMultiplier) * placeableItem.height}% - 20px)`;
                    } else {
                      dragIndicatorRef.current.style.display = 'block';
                      dragIndicatorRef.current.style.top = `calc(${(100.0 / rowCount / gridMultiplier) * movingItem.y}% + 10px)`;
                      dragIndicatorRef.current.style.left = `calc(${(100.0 / columnCount / gridMultiplier) * movingItem.x}% + 10px)`;
                      dragIndicatorRef.current.style.width = `calc(${(100.0 / columnCount / gridMultiplier) * movingItem.width}% - 20px)`;
                      dragIndicatorRef.current.style.height = `calc(${(100.0 / rowCount / gridMultiplier) * movingItem.height}% - 20px)`;
                    }
                  }
                }}
                onMouseMove={(event) => {
                  event.stopPropagation();
                  if (dragIndicatorRef.current !== null && movingItem.x !== undefined && movingItem.y !== undefined) {
                    switch (dragResizeMode) {
                      case DragResizeMode.drag:
                        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + event.movementX + 'px';
                        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + event.movementY + 'px';
                        targetX = Math.round((dragIndicatorRef.current.offsetLeft + event.movementX) / cellSize);
                        targetY = Math.round((dragIndicatorRef.current.offsetTop + event.movementY) / cellSize);
                        targetWidth = movingItem.width / gridMultiplier;
                        targetHeight = movingItem.height / gridMultiplier;
                        break;
                      case DragResizeMode.resizeTop:
                        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + event.movementY + 'px';
                        dragIndicatorRef.current.style.height = dragIndicatorRef.current.offsetHeight - event.movementY + 'px';
                        targetX = movingItem.x / gridMultiplier;
                        targetY = Math.round((dragIndicatorRef.current.offsetTop + event.movementY) / cellSize);
                        targetWidth = movingItem.width / gridMultiplier;
                        targetHeight = Math.round((dragIndicatorRef.current.offsetHeight + event.movementY) / cellSize);
                        break;
                      case DragResizeMode.resizeRight:
                        dragIndicatorRef.current.style.width = dragIndicatorRef.current.offsetWidth + event.movementX + 'px';
                        targetX = movingItem.x / gridMultiplier;
                        targetY = movingItem.y / gridMultiplier;
                        targetWidth = Math.round((dragIndicatorRef.current.offsetWidth + event.movementX) / cellSize);
                        targetHeight = movingItem.height / gridMultiplier;
                        break;
                      case DragResizeMode.resizeBottom:
                        dragIndicatorRef.current.style.height = dragIndicatorRef.current.offsetHeight + event.movementY + 'px';
                        targetX = movingItem.x / gridMultiplier;
                        targetY = movingItem.y / gridMultiplier;
                        targetWidth = movingItem.width / gridMultiplier;
                        targetHeight = Math.round((dragIndicatorRef.current.offsetHeight + event.movementX) / cellSize);
                        break;
                      case DragResizeMode.resizeLeft:
                        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + event.movementX + 'px';
                        dragIndicatorRef.current.style.width = dragIndicatorRef.current.offsetWidth - event.movementX + 'px';
                        targetX = Math.round((dragIndicatorRef.current.offsetLeft + event.movementX) / cellSize);
                        targetY = movingItem.y / gridMultiplier;
                        targetWidth = Math.round((dragIndicatorRef.current.offsetWidth + event.movementX) / cellSize);
                        targetHeight = movingItem.height / gridMultiplier;
                        break;
                      case DragResizeMode.place:
                        dragIndicatorRef.current.style.left = dragIndicatorRef.current.offsetLeft + event.movementX + 'px';
                        dragIndicatorRef.current.style.top = dragIndicatorRef.current.offsetTop + event.movementY + 'px';
                        targetX = Math.round((dragIndicatorRef.current.offsetLeft + event.movementX) / cellSize);
                        targetY = Math.round((dragIndicatorRef.current.offsetTop + event.movementY) / cellSize);
                        targetWidth = placeableItem.width / gridMultiplier;
                        targetHeight = placeableItem.height / gridMultiplier;
                        break;
                      default:
                        break;
                    }

                    highlightDropArea(targetX, targetY, targetWidth, targetHeight);
                  }
                }}
                onMouseUp={() => {
                  if (itemMoved && movingItem.x !== undefined && movingItem.y !== undefined) {
                    if (targetX < 0 || targetY < 0 || targetX + targetWidth > columnCount || targetY + targetHeight > rowCount) {
                      targetX = movingItem.x;
                      targetY = movingItem.y;
                      targetWidth = movingItem.width;
                      targetHeight = movingItem.height;
                      dispatch(
                        addNotification({
                          text: `Cannot move/resize to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as its out of bounds`,
                          type: AlertType.warning,
                        }),
                      );
                      console.warn(
                        `Cannot move/resize to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as its out of bounds`,
                      );
                      return;
                    }

                    if (targetWidth < 1 || targetHeight < 1) {
                      targetX = movingItem.x;
                      targetY = movingItem.y;
                      targetWidth = movingItem.width;
                      targetHeight = movingItem.height;
                      dispatch(
                        addNotification({
                          text: `Cannot resize to size ${targetWidth},${targetHeight} as its too small`,
                          type: AlertType.warning,
                        }),
                      );
                      console.warn(`Cannot resize to size ${targetWidth},${targetHeight} as its too small`);
                      return;
                    }

                    switch (dragResizeMode) {
                      case DragResizeMode.place:
                        if (highlightDropArea(targetX, targetY, targetWidth, targetHeight)) {
                          dispatch(
                            addDashboardItem({
                              id: movingItem.id,
                              x: targetX * gridMultiplier,
                              y: targetY * gridMultiplier,
                              width: targetWidth * gridMultiplier,
                              height: targetHeight * gridMultiplier,
                              pluginName: placeableItem.pluginName,
                              dataPluginId: defaultDataPlugin ? defaultDataPlugin.id : undefined,
                            }),
                          );
                        } else {
                          dispatch(
                            addNotification({
                              text: `Cannot place to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as it would overlap with a different item`,
                              type: AlertType.warning,
                            }),
                          );
                          console.warn(
                            `Cannot place to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as it would overlap with a different item`,
                          );
                        }
                        break;
                      default:
                        if (highlightDropArea(targetX, targetY, targetWidth, targetHeight)) {
                          dispatch(
                            moveDashboardItem({
                              id: movingItem.id,
                              x: targetX * gridMultiplier,
                              y: targetY * gridMultiplier,
                              width: targetWidth * gridMultiplier,
                              height: targetHeight * gridMultiplier,
                              dataPluginId: defaultDataPlugin ? defaultDataPlugin.id : undefined,
                            }),
                          );
                        } else {
                          dispatch(
                            addNotification({
                              text: `Cannot move to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as it would overlap with a different item`,
                              type: AlertType.warning,
                            }),
                          );
                          console.warn(
                            `Cannot move to position ${targetX},${targetY} with size ${targetWidth},${targetHeight} as it would overlap with a different item`,
                          );
                        }
                        break;
                    }
                  }
                  setItemMoved(false);
                  clearHighlightDropArea(columnCount, rowCount);
                }}
                onMouseLeave={() => {
                  setItemMoved(false);
                  dispatch(setDragResizeMode(DragResizeMode.none));
                  clearHighlightDropArea(columnCount, rowCount);
                }}></div>
            </>
          )}
        </div>
      </div>
    </>
  );
}

export default Dashboard;
