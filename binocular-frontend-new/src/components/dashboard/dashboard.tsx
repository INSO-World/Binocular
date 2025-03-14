import dashboardStyles from './dashboard.module.scss';
import { createRef, useEffect, useRef, useState } from 'react';
import DashboardItem from './dashboardItem/dashboardItem.tsx';
import { DragResizeMode } from './resizeMode.ts';
import { useSelector } from 'react-redux';
import { AppDispatch, RootState, store, useAppDispatch } from '../../redux';
import { addDashboardItem, deleteDashboardItem, moveDashboardItem } from '../../redux/reducer/general/dashboardReducer.ts';
import { SettingsGeneralGridSize } from '../../types/settings/generalSettingsType.ts';
import { DashboardItemDTO, DashboardItemType } from '../../types/general/dashboardItemType.ts';
import { DatabaseSettingsDataPluginType } from '../../types/settings/databaseSettingsType.ts';
import { addNotification } from '../../redux/reducer/general/notificationsReducer.ts';
import { AlertType } from '../../types/general/alertType.ts';
import {
  clearHighlightDropArea,
  highlightDropArea,
  moveDragIndicator,
  moveResizeDashboardItem,
  placeDragIndicator,
  setDragResizeMode,
} from './dashboardHelper.ts';

function Dashboard() {
  const dispatch: AppDispatch = useAppDispatch();

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
  const dragResizeZoneRef = createRef<HTMLDivElement>();
  const [cellSize, setCellSize] = useState(0);
  const movingItem = useRef<DashboardItemDTO>({ id: 0, x: 0, y: 0, width: 0, height: 0 });

  const dragResizeMode = useRef(DragResizeMode.none);

  let targetX = 0;
  let targetY = 0;
  let targetWidth = 0;
  let targetHeight = 0;

  // eslint-disable-next-line prefer-const
  let [dashboardItems, setDashboardItems] = useState(store.getState().dashboard.dashboardItems);
  // eslint-disable-next-line prefer-const
  let [dashboardState, setDashboardState] = useState(store.getState().dashboard.dashboardState);

  const placeableItem = useSelector((state: RootState) => state.dashboard.placeableItem);

  const configuredDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  store.subscribe(() => {
    const newDashboardItems = store.getState().dashboard.dashboardItems;
    const newDashboardState = store.getState().dashboard.dashboardState;
    switch (store.getState().actions.lastAction) {
      case moveDashboardItem.type:
        newDashboardItems.forEach((dashboardItem: DashboardItemType) => {
          moveResizeDashboardItem(dashboardItem, rowCount, gridMultiplier, columnCount);
          dashboardItems = newDashboardItems;
          dashboardState = newDashboardState;
        });
        break;
      case addDashboardItem.type:
      case deleteDashboardItem.type:
        setDashboardItems(newDashboardItems);
        setDashboardState(newDashboardState);
        break;
    }
  });

  const defaultDataPlugin = configuredDataPlugins.filter((dP: DatabaseSettingsDataPluginType) => dP.isDefault)[0];

  function setDragResizeItem(itemId: number, mode: DragResizeMode) {
    movingItem.current = dashboardItems.find((dashboardItem: DashboardItemType) => dashboardItem.id === itemId);
    setDragResizeMode(dragResizeZoneRef, dragResizeMode, mode);
  }

  useEffect(() => {
    if (placeableItem !== undefined) {
      setDragResizeMode(dragResizeZoneRef, dragResizeMode, DragResizeMode.place);
    } else {
      setDragResizeMode(dragResizeZoneRef, dragResizeMode, DragResizeMode.none);
    }
  }, [placeableItem]);

  useEffect(() => {
    if (dashboardRef.current !== null) {
      setCellSize(dashboardRef.current.offsetWidth / columnCount);
    }
  }, [columnCount, gridSize]);

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
                  deleteItem={(itemId) =>
                    dispatch(deleteDashboardItem(dashboardItems.find((dashboardItem: DashboardItemType) => dashboardItem.id === itemId)))
                  }></DashboardItem>
              );
            })}
          </>
          <div className={dashboardStyles.dragIndicator} ref={dragIndicatorRef} id={'dragIndicator'}></div>
          <>
            <div
              ref={dragResizeZoneRef}
              style={{ display: 'none' }}
              className={dashboardStyles.dragResizeZone}
              onMouseEnter={(event) => {
                event.stopPropagation();
                placeDragIndicator(dragIndicatorRef, movingItem, dragResizeMode, columnCount, gridMultiplier, placeableItem, rowCount);
              }}
              onMouseMove={(event) => {
                event.stopPropagation();
                const __ret = moveDragIndicator(
                  dragIndicatorRef,
                  movingItem,
                  dragResizeMode,
                  event,
                  targetX,
                  cellSize,
                  targetY,
                  targetWidth,
                  gridMultiplier,
                  targetHeight,
                  placeableItem,
                  dashboardState,
                  rowCount,
                  columnCount,
                );
                targetX = __ret.targetX;
                targetY = __ret.targetY;
                targetWidth = __ret.targetWidth;
                targetHeight = __ret.targetHeight;
              }}
              onMouseUp={() => {
                if (movingItem.current.x !== undefined && movingItem.current.y !== undefined) {
                  if (targetX < 0 || targetY < 0 || targetX + targetWidth > columnCount || targetY + targetHeight > rowCount) {
                    targetX = movingItem.current.x;
                    targetY = movingItem.current.y;
                    targetWidth = movingItem.current.width;
                    targetHeight = movingItem.current.height;
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
                    targetX = movingItem.current.x;
                    targetY = movingItem.current.y;
                    targetWidth = movingItem.current.width;
                    targetHeight = movingItem.current.height;
                    dispatch(
                      addNotification({
                        text: `Cannot resize to size ${targetWidth},${targetHeight} as its too small`,
                        type: AlertType.warning,
                      }),
                    );
                    console.warn(`Cannot resize to size ${targetWidth},${targetHeight} as its too small`);
                    return;
                  }

                  switch (dragResizeMode.current) {
                    case DragResizeMode.place:
                      if (
                        highlightDropArea(
                          movingItem,
                          dashboardState,
                          rowCount,
                          columnCount,
                          gridMultiplier,
                          targetX,
                          targetY,
                          targetWidth,
                          targetHeight,
                        )
                      ) {
                        dispatch(
                          addDashboardItem({
                            id: movingItem.current.id,
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
                      if (
                        highlightDropArea(
                          movingItem,
                          dashboardState,
                          rowCount,
                          columnCount,
                          gridMultiplier,
                          targetX,
                          targetY,
                          targetWidth,
                          targetHeight,
                        )
                      ) {
                        dispatch(
                          moveDashboardItem({
                            id: movingItem.current.id,
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
                clearHighlightDropArea(dragIndicatorRef, columnCount, rowCount);
              }}
              onMouseLeave={() => {
                setDragResizeMode(dragResizeZoneRef, dragResizeMode, DragResizeMode.none);
                clearHighlightDropArea(dragIndicatorRef, columnCount, rowCount);
              }}></div>
          </>
        </div>
      </div>
    </>
  );
}

export default Dashboard;
