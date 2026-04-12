import type { Store } from '@reduxjs/toolkit';
import { useState, useEffect } from 'react';
import { setShowActivityTimeline } from '../reducer';

export interface RepositoryActivitySettings {
  showActivityTimeline: boolean;
}

function Settings(props: {
  settings: RepositoryActivitySettings;
  setSettings: (newSettings: RepositoryActivitySettings) => void;
  store?: Store;
}) {
  const [showActivityTimeLine, setShowActivityTimelineLocal] = useState(props.settings.showActivityTimeline);
  const storeDispatch = props.store?.dispatch;

  // Subscribe to store changes to keep toggle in sync
  useEffect(() => {
    if (!props.store) return;
    const unsubscribe = props.store.subscribe(() => {
      const state = props.store!.getState();
      setShowActivityTimelineLocal(state.plugin.showActivityTimeline);
    });
    return unsubscribe;
  }, [props.store]);

  const handleShowActivityTimeline = (newValue: boolean) => {
    props.setSettings({ showActivityTimeline: newValue });

    if (storeDispatch) {
      storeDispatch(setShowActivityTimeline(newValue));
    }
  };
  return (
    <>
      <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
        <span className="label-text">Show Activity Timeline:</span>
        <input
          type="checkbox"
          className="toggle toggle-primary toggle-sm"
          checked={showActivityTimeLine}
          onChange={(event) => handleShowActivityTimeline(event.target.checked)}
        />
      </label>
    </>
  );
}

export default Settings;
