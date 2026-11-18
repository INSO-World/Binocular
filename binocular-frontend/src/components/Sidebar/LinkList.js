'use strict';

import cx from 'classnames';
import React from 'react';
import styles from './sidebar.module.scss';
import PanelLink from './PanelLink';

export default function LinkList({ entries, selectedViz, onToggleGroup, onSelect }) {
  return (
    <div>
      {entries.map(({ group, expanded, visualization, nested }, index) =>
        group ? (
          <button
            key={group}
            type="button"
            className={cx(
              styles.link,
              styles.groupLink,
              index % 2 === 0 && styles.oddLink,
              selectedViz === index && styles.keyboardSelection,
            )}
            aria-expanded={expanded}
            data-keyboard-selected={selectedViz === index ? '' : undefined}
            onClick={() => onToggleGroup(group)}>
            <span>{group}</span>
            <span aria-hidden="true">{expanded ? '▾' : '▸'}</span>
          </button>
        ) : (
          <PanelLink
            key={visualization.id}
            visualization={visualization}
            nested={nested}
            odd={index % 2 === 0}
            pressReturnToSelect={index === selectedViz}
            onSelect={onSelect}
          />
        ),
      )}
    </div>
  );
}
