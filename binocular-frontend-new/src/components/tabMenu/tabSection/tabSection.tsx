import tabStyles from './tabSection.module.scss';
import React, { type ReactElement } from 'react';
import { TabAlignment } from '../../../types/general/tabType.ts';

export interface TabSectionProps {
  children: ReactElement[] | ReactElement;
  alignment?: TabAlignment;
  name?: string;
}
function TabSection(props: TabSectionProps) {
  if (props.alignment === undefined || props.alignment === TabAlignment.top || props.alignment === TabAlignment.bottom) {
    return (
      <div className={tabStyles.tabSectionHorizontal}>
        <div className={tabStyles.tabSectionName}>{props.name}</div>
        {Array.isArray(props.children)
          ? props.children.map((child) => React.cloneElement(child as ReactElement<{ orientation: string }>, { orientation: 'horizontal' }))
          : React.cloneElement(props.children as ReactElement<{ orientation: string }>, { orientation: 'horizontal' })}
      </div>
    );
  } else {
    return (
      <div className={tabStyles.tabSectionVertical}>
        <div className={tabStyles.tabSectionName}>{props.name}</div>
        {Array.isArray(props.children)
          ? props.children.map((child) => React.cloneElement(child as ReactElement<{ orientation: string }>, { orientation: 'vertical' }))
          : React.cloneElement(props.children as ReactElement<{ orientation: string }>, { orientation: 'vertical' })}
      </div>
    );
  }
}

export default TabSection;
