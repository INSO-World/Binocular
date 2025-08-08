import tabStyles from './tab.module.scss';
import React, { type ReactElement } from 'react';
import TabSection, { type TabSectionProps } from '../tabSection/tabSection.tsx';
import { TabAlignment } from '../../../types/general/tabType.ts';
import type { TabElementType } from '../tabController/tabController';

export interface TabProps {
  children: ReactElement[] | ReactElement;
  displayName: string;
  alignment: TabAlignment;
}

function Tab(props: TabProps) {
  console.log('Tab', props);
  if (Array.isArray(props.children)) {
    return (
      <div className={tabStyles.tab}>
        {props.children.map((child, i) => {
          if ((child.type as TabElementType).name === TabSection.name) {
            return React.cloneElement(child as ReactElement<TabSectionProps>, {
              key: props.displayName + 'Section' + i,
              alignment: props.alignment,
            });
          }
          return child;
        })}
      </div>
    );
  }
  return (
    <div className={tabStyles.tab}>
      {(props.children.type as TabElementType).name === TabSection.name
        ? React.cloneElement(props.children as ReactElement<TabSectionProps>, {
            alignment: props.alignment,
          })
        : props.children}
    </div>
  );
}

export default Tab;
