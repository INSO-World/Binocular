import { useEffect, useState } from 'react';
import type { Store } from '@reduxjs/toolkit';
import { ApolloClient, HttpLink, InMemoryCache, gql } from '@apollo/client';

export interface SettingsType {
  repoPath: string;
  parentModule: string; // aktuelle Ebene ('.' = Wurzel)
  neededModules: string[]; // abgeleitet: die direkten Kinder von parentModule
}

const client = new ApolloClient({
  link: new HttpLink({ uri: '/graphQl' }),
  cache: new InMemoryCache(),
  defaultOptions: { watchQuery: { fetchPolicy: 'no-cache' } },
});

// './ui/components' -> ['ui','components']; '.' und leere Teile ignorieren
function segments(module: string): string[] {
  return module.split('/').filter((s) => s.length > 0 && s !== '.');
}
// direkte Kinder = genau eine Ebene tiefer und mit dem Pfad-Prefix des Elternteils
function directChildren(all: string[], parent: string): string[] {
  const p = segments(parent);
  return all.filter((m) => {
    const s = segments(m);
    return s.length === p.length + 1 && p.every((seg, i) => s[i] === seg);
  });
}

function Settings(props: { settings: SettingsType; setSettings: (s: SettingsType) => void; store?: Store }) {
  const [modules, setModules] = useState<string[]>([]);

  // Alle Module einmal laden (neededModules: [] = alle) und die aktuelle Ebene ableiten
  useEffect(() => {
    const repoPath = props.settings.repoPath;
    if (!repoPath) return;
    const dr = props.store?.getState().plugin.dateRange ?? { from: '2000-01-01', to: new Date().toISOString() };
    client
      .query({
        query: gql`
          query ($repoPath: String!, $since: Timestamp!, $until: Timestamp!) {
            moduleSizeChangeFrequency(repoPath: $repoPath, since: $since, until: $until, neededModules: []) {
              module
            }
          }
        `,
        variables: { repoPath, since: new Date(dr.from).getTime(), until: new Date(dr.to).getTime() },
      })
      .then((res) => {
        const all = [...new Set((res.data.moduleSizeChangeFrequency as { module: string }[]).map((r) => r.module))].sort();
        setModules(all);
        const parent = props.settings.parentModule ?? '.';
        props.setSettings({ ...props.settings, parentModule: parent, neededModules: directChildren(all, parent) });
      })
      .catch((e) => console.error(e));
  }, [props.settings.repoPath, props.store]);

  // Ebene wechseln und die anzuzeigenden Module daraus ableiten
  function selectParent(parent: string) {
    props.setSettings({ ...props.settings, parentModule: parent, neededModules: directChildren(modules, parent) });
  }

  // nur Module anbieten, die tatsächlich Kinder haben (plus die Wurzel)
  const parentOptions = ['.', ...modules.filter((m) => directChildren(modules, m).length > 0)];

  return (
    <div>
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

      <div className="font-bold mt-2">Module level:</div>
      <div className="flex gap-1 items-center mt-0.5">
        <button
          className="btn btn-xs"
          disabled={!props.settings.parentModule || props.settings.parentModule === '.'}
          onClick={() => {
            const s = segments(props.settings.parentModule ?? '.');
            selectParent(s.length <= 1 ? '.' : `./${s.slice(0, -1).join('/')}`);
          }}>
          ↑
        </button>
        <select
          className="select select-bordered select-xs flex-1"
          value={props.settings.parentModule ?? '.'}
          onChange={(e) => selectParent(e.target.value)}>
          {parentOptions.map((m) => (
            <option key={m} value={m}>
              {m === '.' ? '. (root)' : m}
            </option>
          ))}
        </select>
      </div>
      <div className="mt-1 text-xs opacity-70">Showing {props.settings.neededModules?.length ?? 0} direct submodule(s)</div>
    </div>
  );
}
export default Settings;
