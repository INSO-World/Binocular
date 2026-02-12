import type { DataPluginBranch } from '../../../../../interfaces/dataPluginInterfaces/dataPluginBranches.ts';
import { useCallback, useMemo, useState, useSyncExternalStore, type JSX } from 'react';
import { toNumber } from 'lodash';
import type { Store } from '@reduxjs/toolkit';

export interface CodeOwnerShipSettings {
  displayMode: string;
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
  const [branchOptions, setBranchOptions] = useState<JSX.Element[]>([
    <option key={-1} value={''}>
      Select a Branch
    </option>,
  ]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const getBranchOptions = useMemo(() => {
    if (allBranches.length === 0) {
      return;
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
    setBranchOptions(options);
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
                currentBranch: props.settings.currentBranch,
              })
            }>
            <option value={'absolute'}>absolute</option>
            <option value={'relative'}>relative</option>
          </select>
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Branch:</span>
          <select
            value={props.settings.currentBranch != undefined ? props.settings.currentBranch : ''}
            className="select select-bordered select-xs w-36"
            onChange={(e) => {
              if (e.target.value != '')
                props.setSettings({
                  displayMode: props.settings.displayMode,
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
