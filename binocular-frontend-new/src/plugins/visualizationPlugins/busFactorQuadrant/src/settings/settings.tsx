import { useEffect, useState } from 'react';
import type { Store } from '@reduxjs/toolkit';
import { ApolloClient, HttpLink, InMemoryCache, gql } from '@apollo/client';

// Settings for this widget. These are persisted per dashboard item.
export interface SettingsType {
  repoPath: string;
  busFactorThreshold: number; // y-split between "high" and "low" bus factor
  ciErrorThreshold: number; // x-split between "low" and "high" CI error rate (0..1)
  excludedAuthors: string[]; // authors whose commits should not count
  neededModules: string[]; // modules to show (empty = all)
}

// Own Apollo client, used here only to load the list of available modules/authors.
const client = new ApolloClient({
  link: new HttpLink({ uri: '/graphQl' }),
  cache: new InMemoryCache(),
  defaultOptions: { watchQuery: { fetchPolicy: 'no-cache' } },
});

// Small helper: add the value if it's not in the list, otherwise remove it (checkbox toggle)
function toggle(list: string[], value: string): string[] {
  return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
}

function Settings(props: { settings: SettingsType; setSettings: (s: SettingsType) => void; store?: Store }) {
  // The full list of options to choose from (loaded from the backend below)
  const [modules, setModules] = useState<string[]>([]);
  const [authors, setAuthors] = useState<string[]>([]);

  // Load ALL modules and authors once, unfiltered (excludedAuthors/neededModules = "").
  // We query unfiltered on purpose so the user can always re-enable an excluded author.
  useEffect(() => {
    const repoPath = props.settings.repoPath;
    if (!repoPath) return;
    // Reuse the date range the chart already put into the store; fall back to a wide range
    const dr = props.store?.getState().plugin.dateRange ?? { from: '2000-01-01', to: new Date().toISOString() };
    client
      .query({
        query: gql`
          query ($repoPath: String!, $since: Timestamp!, $until: Timestamp!) {
            busFactorCIErrorRateModules(repoPath: $repoPath, since: $since, until: $until, excludedAuthors: "", neededModules: "") {
              module
              topAuthors {
                gitSignature
              }
            }
          }
        `,
        variables: { repoPath, since: new Date(dr.from).getTime(), until: new Date(dr.to).getTime() },
      })
      .then((res) => {
        const rows = res.data.busFactorCIErrorRateModules as { module: string; topAuthors: { gitSignature: string }[] }[];
        // Build unique, sorted lists for the checkboxes (Set removes duplicates)
        setModules([...new Set(rows.map((r) => r.module))].sort());
        setAuthors([...new Set(rows.flatMap((r) => r.topAuthors.map((a) => a.gitSignature)))].sort());
      })
      .catch((e) => console.error(e));
  }, [props.settings.repoPath, props.store]);

  return (
    <div>
      {/* Repository to query. onBlur so we only save once the user leaves the field */}
      <label className="label flex w-full justify-between items-center mt-0.5">
        <span className="label-text">Repository path:</span>
        <input
          type="text"
          className="input input-bordered input-xs w-40"
          placeholder="mein-repo"
          defaultValue={props.settings.repoPath}
          onBlur={(e) => props.setSettings({ ...props.settings, repoPath: e.target.value })}
        />
      </label>

      {/* Checkbox list of authors to exclude from the calculation */}
      <div className="font-bold mt-2">Exclude authors:</div>
      <div className="max-h-32 overflow-auto border border-base-300 rounded p-1">
        {authors.map((a) => (
          <label key={a} className="label cursor-pointer flex justify-between gap-2 py-0">
            {/* title shows the full signature on hover; the visible text drops the email */}
            <span className="label-text truncate" title={a}>
              {a.replace(/\s*<.*>$/, '')}
            </span>
            <input
              type="checkbox"
              className="checkbox checkbox-xs"
              checked={props.settings.excludedAuthors?.includes(a) ?? false}
              onChange={() => props.setSettings({ ...props.settings, excludedAuthors: toggle(props.settings.excludedAuthors ?? [], a) })}
            />
          </label>
        ))}
      </div>

      {/* Checkbox list to restrict which modules are shown (none checked = all) */}
      <div className="font-bold mt-2">Modules (empty = all):</div>
      <div className="max-h-32 overflow-auto border border-base-300 rounded p-1">
        {modules.map((m) => (
          <label key={m} className="label cursor-pointer flex justify-between gap-2 py-0">
            {/* title shows the full module path on hover in case it gets truncated */}
            <span className="label-text truncate" title={m}>
              {m}
            </span>
            <input
              type="checkbox"
              className="checkbox checkbox-xs"
              checked={props.settings.neededModules?.includes(m) ?? false}
              onChange={() => props.setSettings({ ...props.settings, neededModules: toggle(props.settings.neededModules ?? [], m) })}
            />
          </label>
        ))}
      </div>
    </div>
  );
}
export default Settings;
