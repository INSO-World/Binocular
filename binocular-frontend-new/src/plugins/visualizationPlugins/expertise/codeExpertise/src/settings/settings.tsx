import type { DataPluginBranch } from '../../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';
import { useCallback, useMemo, useSyncExternalStore } from 'react';
import { toNumber } from 'lodash';
import type { Store } from '@reduxjs/toolkit';

export interface BranchSettings {
  allBranches: DataPluginBranch[];
  currentBranch?: number;
}

const EMPTY_BRANCHES: DataPluginBranch[] = [];

function Settings(props: { settings: BranchSettings; setSettings: (newSettings: BranchSettings) => void; store?: Store }) {
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
        <label className="form-control w-full max-w-xs">
          <div className="label">
            <span className="label-text">Branch:</span>
          </div>
          <select
            value={props.settings.currentBranch ? props.settings.currentBranch : ''}
            className="select select-bordered select-sm"
            onChange={(e) => {
              props.setSettings({
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
