'use strict';

import styles from './sidebar.module.scss';
import menu from './assets/menu.svg';
import menuWhite from './assets/menuWhite.svg';
import hideSidebar from './assets/hideSidebar.svg';

import cx from 'classnames';
import React from 'react';
import LinkList from './LinkList';
import UniversalSettings from '../UniversalSettings/universalSettings';
import { getContext } from '../../utils/context';

const ctx = getContext();

function navigationEntries(visualizations, activeVisualization, searching, search, expandedGroups) {
  const term = search.trim().toLowerCase();
  const available = Object.values(visualizations).filter((vis) => searching || vis.id !== activeVisualization);
  if (term) {
    return available
      .filter((vis) => [vis.id, vis.label, vis.navigationGroup].some((value) => value?.toLowerCase().includes(term)))
      .map((visualization) => ({ visualization }));
  }

  const entries = [];
  const groups = new Map();
  for (const visualization of available) {
    const group = visualization.navigationGroup;
    if (!group) {
      entries.push({ visualization });
    } else {
      if (!groups.has(group)) groups.set(group, []);
      groups.get(group).push({ visualization, nested: true });
    }
  }
  const groupedEntries = [...groups].flatMap(([group, members]) => {
    const expanded = expandedGroups.includes(group);
    return [{ group, expanded }, ...(expanded ? members : [])];
  });
  return [...entries, ...groupedEntries];
}

export default class Sidebar extends React.Component {
  state = { searching: false, search: '', selectedViz: 0, expandedGroups: [] };

  toggleGroup = (group) => {
    this.setState(({ expandedGroups }) => ({
      expandedGroups: expandedGroups.includes(group) ? expandedGroups.filter((name) => group !== name) : [...expandedGroups, group],
    }));
  };

  closeSelector = () => this.setState({ searching: false, search: '', selectedViz: 0 });

  handleSearchKey = (event, entries) => {
    const { selectedViz } = this.state;
    if (event.key === 'Enter') {
      const selected = entries[selectedViz];
      if (selected?.group) this.toggleGroup(selected.group);
      if (selected?.visualization) {
        this.props.switchVisualization(selected.visualization.id);
        this.closeSelector();
      }
    } else if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
      event.preventDefault();
      const offset = event.key === 'ArrowUp' ? -1 : 1;
      this.setState({ selectedViz: Math.max(0, Math.min(entries.length - 1, selectedViz + offset)) }, () => {
        this.selectorOptions?.querySelector('[data-keyboard-selected]')?.scrollIntoView?.({ block: 'nearest' });
      });
    } else if (event.key === 'Escape') {
      this.closeSelector();
      this.selectorToggle?.focus();
    }
  };

  render() {
    const { visualizations, activeVisualization, onToggle, collapsed } = this.props;
    const { searching, search, selectedViz, expandedGroups } = this.state;
    const active = visualizations[activeVisualization];
    const ConfigComponent = active.ConfigComponent;
    const entries = navigationEntries(visualizations, activeVisualization, searching, search, expandedGroups);

    if (collapsed) {
      return (
        <div className={styles.showSidebar} onClick={onToggle}>
          <img src={menuWhite} />
        </div>
      );
    }

    return (
      <div className={styles.sidebar}>
        <div className={styles.headline} onClick={onToggle}>
          <div className={styles.headlineText}>
            <div>
              <div className={styles.headlineHint}>Repository:</div>
              <div>{ctx.repo.name}</div>
            </div>
          </div>
          <div className={styles.hideConfig}>
            <img src={hideSidebar} />
          </div>
        </div>
        <div className={styles.sidebarInnerContainer}>
          <nav aria-label="Visualizations">
            <div className={styles.visualizationSelectorContainer}>
              <div className={cx(styles.visualizationSelector, searching && styles.visualizationSelectorExtended)}>
                <div className={styles.visualizationSelectorCurrentVisualization}>
                  <button
                    type="button"
                    ref={(button) => {
                      this.selectorToggle = button;
                    }}
                    className={styles.visualizationSelectorToggle}
                    aria-label={searching ? 'Close visualization search' : `Choose visualization. Current: ${active.label}`}
                    aria-expanded={searching}
                    onClick={() => (searching ? this.closeSelector() : this.setState({ searching: true, selectedViz: 0 }))}>
                    <span className={styles.visualizationSelectorIcon}>
                      <img src={menu} alt="" />
                    </span>
                    {!searching && (
                      <span className={styles.visualizationSelectorText} title={active.label}>
                        {active.label}
                      </span>
                    )}
                  </button>
                  {searching && (
                    <input
                      className={styles.visualizationSelectorSearch}
                      autoFocus
                      aria-label="Search visualizations"
                      placeholder="Search"
                      value={search}
                      type="search"
                      onChange={(event) => this.setState({ search: event.target.value, selectedViz: 0 })}
                      onKeyDown={(event) => this.handleSearchKey(event, entries)}
                    />
                  )}
                </div>
                <div
                  ref={(element) => {
                    this.selectorOptions = element;
                  }}
                  className={styles.visualizationSelectorVisualizationsContainer}>
                  <LinkList
                    entries={entries}
                    selectedViz={searching ? selectedViz : -1}
                    onToggleGroup={this.toggleGroup}
                    onSelect={this.closeSelector}
                  />
                </div>
              </div>
            </div>
          </nav>
          <div className={styles.configSection}>
            {searching && <div className={styles.visualizationSelectorOutsideDetector} onClick={this.closeSelector} />}
            {active.usesUniversalSettings && (
              <>
                <div className={cx('panel-block', styles.configuration)}>
                  <h1>Universal Settings:</h1>
                </div>
                <div className={cx('panel-block', styles.configuration)}>
                  <UniversalSettings universalSettingsConfig={active.universalSettingsConfig} />
                </div>
                <div className={cx('panel-block', styles.configuration)}>
                  <h1>Visualization Specific Settings:</h1>
                </div>
              </>
            )}
            <div className={cx('panel-block', styles.configuration)}>
              <ConfigComponent />
            </div>
          </div>
        </div>
      </div>
    );
  }
}
