import type { DataPluginBranch } from '../../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';
import { useCallback, useMemo, useSyncExternalStore } from 'react';
import { toNumber } from 'lodash';
import type { Store } from '@reduxjs/toolkit';

export interface CodeOwnerShipSettings {
  displayMode: string;
  allBranches: DataPluginBranch[];
  currentBranch?: number;
}

const EMPTY_BRANCHES: DataPluginBranch[] = [];

function Settings(props: { settings: CodeOwnerShipSettings; setSettings: (newSettings: CodeOwnerShipSettings) => void; store?: Store }) {
  const subscribe = useCallback(
    (callback: () => void) => {
      if (!props.store) return () => {};
      return props.store.subscribe(callback);
    },
    [props.store],
  );

  const getSnapshot = useCallback(() => {
    return props.store?.getState()?.plugin?.allBranches ?? EMPTY_BRANCHES;
  }, [props.store]);

  const allBranches: DataPluginBranch[] = useSyncExternalStore(subscribe, getSnapshot);

  const branchOptions = useMemo(() => {
    if (allBranches.length === 0) {
      return [
        <option key={-1} value={''}>
          Select a Branch
        </option>,
      ];
    }
    const sorted = [...allBranches].sort((a, b) => a.branch.localeCompare(b.branch)).map((b) => b.branch);
    const options = [
      <option key={-1} value={''}>
        Select a Branch
      </option>,
    ];
    sorted.forEach((value: string, index: number) => {
      options.push(
        <option key={index} value={index}>
          {value}
        </option>,
      );
    });
    return options;
  }, [allBranches]);

  return (
    <>
      <div>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Display Mode:</span>
          <select
            className={'select select-bordered select-xs w-24'}
            defaultValue={props.settings.displayMode}
            onChange={(e) =>
              props.setSettings({
                displayMode: e.target.value,
                allBranches: allBranches,
              })
            }>
            <option value={'absolute'}>absolute</option>
            <option value={'relative'}>relative</option>
          </select>
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Branch:</span>
          <select
            value={props.settings.currentBranch ? props.settings.currentBranch : ''}
            className="select select-bordered select-xs w-36"
            onChange={(e) => {
              props.setSettings({
                displayMode: props.settings.displayMode,
                allBranches: allBranches,
                currentBranch: toNumber(e.target.value),
              });
            }}>
            {branchOptions}
          </select>
        </label>
      </div>
    </>
  );
}

export default Settings;
