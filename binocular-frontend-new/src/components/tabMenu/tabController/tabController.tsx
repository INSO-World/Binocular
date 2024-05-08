import { ReactElement, useState } from 'react';
import tabControllerStyles from './tabController.module.scss';
import tabHandleStyles from './tabHandle.module.scss';
import TabDropHint from './tabDropHint/tabDropHint.tsx';
import Tab from '../tab/tab.tsx';
import TabMenuContent from '../tabMenuContent/tabMenuContent.tsx';

interface TabType {
  selected: boolean;
  content: ReactElement;
  displayName: string;
  alignment: string;
  position: number;
}

/**
 * Helper Function to generate the click and draggable handle of each tab
 * @param tab Tab object that includes all necessary information about a tab
 * @param tabList List of all tabs
 * @param setTabList Set state function for a list of all tabs
 * @param setDragState Set state Function for drag and drop state of tabs
 */
function generateHandel(
  tab: TabType,
  tabList: TabType[],
  setTabList: (newTabList: TabType[]) => void,
  setDragState: (dragState: boolean) => void,
) {
  return (
    <div
      key={tab.displayName}
      draggable={true}
      id={'tab_' + tab.displayName}
      className={
        (tab.alignment === 'left' || tab.alignment === 'right'
          ? tabHandleStyles.tabHandel + ' ' + tabHandleStyles.tabHandelVertical
          : tabHandleStyles.tabHandel) +
        (tab.selected
          ? ' ' +
            (tab.alignment === 'right'
              ? tabHandleStyles.tabHandelSelectedVertical
              : tab.alignment === 'bottom'
                ? tabHandleStyles.tabHandelSelectedBottom
                : tab.alignment === 'left'
                  ? tabHandleStyles.tabHandelSelectedVertical
                  : tabHandleStyles.tabHandelSelectedTop)
          : '')
      }
      onClick={() => {
        setTabList(
          tabList.map((listTab) => {
            if (listTab.alignment === tab.alignment) {
              if (listTab.displayName === tab.displayName) {
                listTab.selected = !listTab.selected;
                document.getElementById('tab_' + listTab.displayName)?.classList.add(tabHandleStyles.tabHandelSelected);
              } else {
                listTab.selected = false;
                document.getElementById('tab_' + listTab.displayName)?.classList.remove(tabHandleStyles.tabHandelSelected);
              }
            }
            return listTab;
          }),
        );
      }}
      onDragStart={(event) => {
        console.log(`Dragging: ${tab.displayName}`);
        setDragState(true);
        event.dataTransfer.clearData();
        event.dataTransfer.setData('text/plain', tab.displayName);
      }}
      onDragEnd={(event) => {
        setDragState(false);
        event.dataTransfer.clearData();
      }}
      onDragOver={(event) => {
        event.stopPropagation();
        event.preventDefault();
      }}
      onDragEnter={() => {
        document.getElementById('tab_' + tab.displayName)?.classList.add(tabHandleStyles.tabHandelSwitch);
      }}
      onDragLeave={() => {
        document.getElementById('tab_' + tab.displayName)?.classList.remove(tabHandleStyles.tabHandelSwitch);
      }}
      onDrop={(event) => {
        event.stopPropagation();
        document.getElementById('tab_' + tab.displayName)?.classList.remove(tabHandleStyles.tabHandelSwitch);
        switchTabs(event.dataTransfer.getData('text/plain'), tab.displayName, tabList, setTabList, setDragState);
      }}>
      {tab.displayName}
    </div>
  );
}

/**
 * Moves the tab to a different side
 * @param name Name of the tab
 * @param alignment New alignment of the tab
 * @param tabList List of all tabs
 * @param setTabList Set state function for a list of all tabs
 * @param setDragState Set state Function for drag and drop state of tabs
 */
function moveTab(
  name: string,
  alignment: string,
  tabList: TabType[],
  setTabList: (newTabList: TabType[]) => void,
  setDragState: (dragState: boolean) => void,
) {
  console.log(`Moving tab ${name} to ${alignment}`);
  setDragState(false);
  setTabList(
    tabList.map((tab) => {
      if (tab.alignment === alignment) {
        tab.selected = false;
      }
      if (tab.displayName === name) {
        tab.alignment = alignment;
        tab.selected = true;
      }
      return tab;
    }),
  );
}

/**
 * Switches two tabs with each other
 * @param name Name of the tab that is switched
 * @param targetTabName Name of the other tab with whom the first tab is switched
 * @param tabList List of all tabs
 * @param setTabList Set state function for a list of all tabs
 * @param setDragState Set state Function for drag and drop state of tabs
 */
function switchTabs(
  name: string,
  targetTabName: string,
  tabList: TabType[],
  setTabList: (newTabList: TabType[]) => void,
  setDragState: (dragState: boolean) => void,
) {
  console.log(`Switching tab ${name} with ${targetTabName}`);
  setDragState(false);
  const tab = { ...tabList.filter((t) => t.displayName === name)[0] };
  const targetTab = { ...tabList.filter((t) => t.displayName === targetTabName)[0] };

  setTabList(
    tabList.map((listTab) => {
      if (listTab.displayName === name) {
        listTab.alignment = targetTab.alignment;
        listTab.position = targetTab.position;
        listTab.selected = targetTab.selected;
      }
      if (listTab.displayName === targetTabName) {
        listTab.alignment = tab.alignment;
        listTab.position = tab.position;
        listTab.selected = tab.selected;
      }
      return listTab;
    }),
  );
}

function TabController(props: { children: ReactElement<{ displayName: string; defaultAlignment: string }>[]; appName: string }) {
  const firstFound = [false, false, false, false];
  let tabOrder = 0;
  const [tabList, setTabList] = useState(
    props.children
      .filter((child) => child.type === Tab)
      .map((tab) => {
        const selected =
          (tab.props.defaultAlignment === 'top' && !firstFound[0]) ||
          (tab.props.defaultAlignment === 'right' && !firstFound[1]) ||
          (tab.props.defaultAlignment === 'bottom' && !firstFound[2]) ||
          (tab.props.defaultAlignment === 'left' && !firstFound[3]);
        if (selected) {
          switch (tab.props.defaultAlignment) {
            case 'right':
              firstFound[1] = true;
              break;
            case 'bottom':
              firstFound[2] = true;
              break;
            case 'left':
              firstFound[3] = true;
              break;
            default:
              firstFound[0] = true;
              break;
          }
        }
        return {
          displayName: tab.props.displayName,
          alignment: tab.props.defaultAlignment,
          selected: selected,
          content: tab,
          position: tabOrder++,
        };
      }),
  );
  const [dragState, setDragState] = useState(false);

  const [tabMenuContent] = useState(props.children.filter((child) => child.type === TabMenuContent)[0]);

  const tabBarTopCollapsed = tabList.filter((tab) => tab.alignment === 'top' && tab.selected).length === 0;
  const tabBarRightCollapsed = tabList.filter((tab) => tab.alignment === 'right' && tab.selected).length === 0;
  const tabBarBottomCollapsed = tabList.filter((tab) => tab.alignment === 'bottom' && tab.selected).length === 0;
  const tabBarLeftCollapsed = tabList.filter((tab) => tab.alignment === 'left' && tab.selected).length === 0;

  const tabCountRight = tabList.filter((tab) => tab.alignment === 'right').length;
  const tabCountBottom = tabList.filter((tab) => tab.alignment === 'bottom').length;
  const tabCountLeft = tabList.filter((tab) => tab.alignment === 'left').length;

  return (
    <div className={tabControllerStyles.tabController}>
      <TabDropHint dragState={dragState}></TabDropHint>
      <>
        <div
          className={tabControllerStyles.content}
          style={{
            top: `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} * ${tabBarTopCollapsed ? 0 : 1} + 4px)`,
            left: `calc(${tabControllerStyles.tabBarThickness} * ${tabCountLeft > 0 ? 1 : 0} + ${tabControllerStyles.tabContentThicknessVertical} * ${tabBarLeftCollapsed ? 0 : 1} + 4px)`,
            height: `calc(100% - ${tabControllerStyles.tabContentThicknessHorizontal} * ${(tabBarTopCollapsed ? 0 : 1) + (tabBarBottomCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${tabCountBottom > 0 ? 2 : 1} - 10px)`,
            width: `calc(100% -  ${tabControllerStyles.tabContentThicknessVertical} * ${(tabBarLeftCollapsed ? 0 : 1) + (tabBarRightCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${(tabCountLeft > 0 ? 1 : 0) + (tabCountRight > 0 ? 1 : 0)} - 8px)`,
          }}>
          {tabMenuContent}
        </div>
      </>
      <>
        <div
          className={
            tabControllerStyles.tabContentBackgroundTop + (tabBarTopCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')
          }></div>
        <div
          className={
            tabControllerStyles.tabContentBackgroundRight + (tabBarRightCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')
          }
          style={{
            top: `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} * ${tabBarTopCollapsed ? 0 : 1} + 4px)`,
            height: `calc(100% - ${tabControllerStyles.tabContentThicknessHorizontal} * ${(tabBarTopCollapsed ? 0 : 1) + (tabBarBottomCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${tabCountBottom > 0 ? 2 : 1} - 10px)`,
          }}></div>
        <div
          className={
            tabControllerStyles.tabContentBackgroundBottom + (tabBarBottomCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')
          }></div>
        <div
          className={
            tabControllerStyles.tabContentBackgroundLeft + (tabBarLeftCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')
          }
          style={{
            top: `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} * ${tabBarTopCollapsed ? 0 : 1} + 4px)`,
            height: `calc(100% - ${tabControllerStyles.tabContentThicknessHorizontal} * ${(tabBarTopCollapsed ? 0 : 1) + (tabBarBottomCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${tabCountBottom > 0 ? 2 : 1} - 10px)`,
          }}></div>
      </>
      <>
        <div
          id={'tabBarTop'}
          className={tabControllerStyles.tabBar + ' ' + tabControllerStyles.tabBarHorizontal + ' ' + tabControllerStyles.tabBarTop}
          onDragOver={(event) => {
            event.stopPropagation();
            event.preventDefault();
          }}
          onDrop={(event) => {
            moveTab(event.dataTransfer.getData('text/plain'), 'top', tabList, setTabList, setDragState);
          }}>
          <div className={tabControllerStyles.appName}>{props.appName}</div>

          {tabList
            .filter((tab) => tab.alignment === 'top')
            .sort((tabA, tabB) => tabA.position - tabB.position)
            .map((tab) => generateHandel(tab, tabList, setTabList, setDragState))}
        </div>
        <div
          id={'tabBarRight'}
          className={tabControllerStyles.tabBar + ' ' + tabControllerStyles.tabBarVertical + ' ' + tabControllerStyles.tabBarRight}
          style={{
            top: `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} * ${tabBarTopCollapsed ? 0 : 1} + 4px)`,
            height: `calc(100% - ${tabControllerStyles.tabContentThicknessHorizontal} * ${(tabBarTopCollapsed ? 0 : 1) + (tabBarBottomCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${tabCountBottom > 0 ? 2 : 1} - 10px)`,
          }}
          onDragOver={(event) => {
            event.stopPropagation();
            event.preventDefault();
          }}
          onDrop={(event) => {
            moveTab(event.dataTransfer.getData('text/plain'), 'right', tabList, setTabList, setDragState);
          }}>
          {tabList
            .filter((tab) => tab.alignment === 'right')
            .sort((tabA, tabB) => tabA.position - tabB.position)
            .map((tab) => generateHandel(tab, tabList, setTabList, setDragState))}
        </div>
        <div
          id={'tabBarBottom'}
          className={tabControllerStyles.tabBar + ' ' + tabControllerStyles.tabBarHorizontal + ' ' + tabControllerStyles.tabBarBottom}
          onDragOver={(event) => {
            event.stopPropagation();
            event.preventDefault();
          }}
          onDrop={(event) => {
            moveTab(event.dataTransfer.getData('text/plain'), 'bottom', tabList, setTabList, setDragState);
          }}>
          {tabList
            .filter((tab) => tab.alignment === 'bottom')
            .sort((tabA, tabB) => tabA.position - tabB.position)
            .map((tab) => generateHandel(tab, tabList, setTabList, setDragState))}
        </div>
        <div
          id={'tabBarLeft'}
          className={tabControllerStyles.tabBar + ' ' + tabControllerStyles.tabBarVertical + ' ' + tabControllerStyles.tabBarLeft}
          style={{
            top: `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} * ${tabBarTopCollapsed ? 0 : 1} + 4px)`,
            height: `calc(100% - ${tabControllerStyles.tabContentThicknessHorizontal} * ${(tabBarTopCollapsed ? 0 : 1) + (tabBarBottomCollapsed ? 0 : 1)} - ${tabControllerStyles.tabBarThickness} * ${tabCountBottom > 0 ? 2 : 1} - 10px)`,
          }}
          onDragOver={(event) => {
            event.stopPropagation();
            event.preventDefault();
          }}
          onDrop={(event) => {
            moveTab(event.dataTransfer.getData('text/plain'), 'left', tabList, setTabList, setDragState);
          }}>
          {tabList
            .filter((tab) => tab.alignment === 'left')
            .sort((tabA, tabB) => tabA.position - tabB.position)
            .map((tab) => generateHandel(tab, tabList, setTabList, setDragState))}
        </div>
      </>
      <>
        <div className={tabControllerStyles.tabContentTop + (tabBarTopCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')}>
          <div className={tabControllerStyles.tabContent}>
            {tabList.filter((tab) => tab.alignment === 'top' && tab.selected)[0]?.content}
          </div>
        </div>
        <div
          className={tabControllerStyles.tabContentRight + (tabBarRightCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')}
          style={{
            top: tabBarTopCollapsed
              ? tabControllerStyles.tabBarThickness
              : `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} + 4px)`,
            height: tabBarTopCollapsed
              ? `calc(100% - 4px - ${tabControllerStyles.tabBarThickness} * 2)`
              : tabBarBottomCollapsed
                ? `calc(calc(100% - 4px  - ${tabControllerStyles.tabBarThickness} * 2 - ${tabControllerStyles.tabContentThicknessHorizontal})`
                : `calc(calc(100% - 4px  - ${tabControllerStyles.tabBarThickness} * 2 - ${tabControllerStyles.tabContentThicknessHorizontal} * 2)`,
          }}>
          <div className={tabControllerStyles.tabContent}>
            {tabList.filter((tab) => tab.alignment === 'right' && tab.selected)[0]?.content}
          </div>
        </div>
        <div
          className={tabControllerStyles.tabContentBottom + (tabBarBottomCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')}>
          <div className={tabControllerStyles.tabContent}>
            {tabList.filter((tab) => tab.alignment === 'bottom' && tab.selected)[0]?.content}
          </div>
        </div>
        <div
          className={tabControllerStyles.tabContentLeft + (tabBarLeftCollapsed ? ' ' + tabControllerStyles.tabContentCollapsed : '')}
          style={{
            top: tabBarTopCollapsed
              ? tabControllerStyles.tabBarThickness
              : `calc(${tabControllerStyles.tabBarThickness} + ${tabControllerStyles.tabContentThicknessHorizontal} + 4px)`,
            height: tabBarTopCollapsed
              ? `calc(100% - 4px - ${tabControllerStyles.tabBarThickness} * 2)`
              : tabBarBottomCollapsed
                ? `calc(calc(100% - 4px  - ${tabControllerStyles.tabBarThickness} * 2 - ${tabControllerStyles.tabContentThicknessHorizontal})`
                : `calc(calc(100% - 4px  - ${tabControllerStyles.tabBarThickness} * 2 - ${tabControllerStyles.tabContentThicknessHorizontal} * 2)`,
          }}>
          <div className={tabControllerStyles.tabContent}>
            {tabList.filter((tab) => tab.alignment === 'left' && tab.selected)[0]?.content}
          </div>
        </div>
      </>
    </div>
  );
}

export default TabController;
