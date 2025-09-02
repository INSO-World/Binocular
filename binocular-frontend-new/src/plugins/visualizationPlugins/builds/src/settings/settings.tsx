import type { DefaultSettings } from '../../../simpleVisualizationPlugin/src/settings/settings';

export interface BuildSettings extends DefaultSettings {
  splitBuildsPerAuthor: boolean;
}

function Settings(props: { settings: BuildSettings; setSettings: (newSettings: BuildSettings) => void }) {
  return (
    <>
      <div>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Show Sprints:</span>
          <input
            type="checkbox"
            className="toggle toggle-accent toggle-sm"
            defaultChecked={props.settings.showSprints}
            onChange={(event) =>
              props.setSettings({
                splitBuildsPerAuthor: props.settings.splitBuildsPerAuthor,
                visualizationStyle: props.settings.visualizationStyle,
                showSprints: event.target.checked,
              })
            }
          />
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Visualization Style:</span>
          <select
            className={'select select-bordered select-xs w-24'}
            defaultValue={props.settings.visualizationStyle}
            onChange={(e) =>
              props.setSettings({
                splitBuildsPerAuthor: props.settings.splitBuildsPerAuthor,
                visualizationStyle: e.target.value,
                showSprints: props.settings.showSprints,
              })
            }>
            <option value={'curved'}>curved</option>
            <option value={'stepped'}>stepped</option>
            <option value={'linear'}>linear</option>
          </select>
        </label>
        <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Split Builds per Author:</span>
          <input
            type="checkbox"
            className="toggle toggle-accent toggle-sm"
            defaultChecked={props.settings.splitBuildsPerAuthor}
            onChange={(event) =>
              props.setSettings({
                splitBuildsPerAuthor: event.target.checked,
                visualizationStyle: props.settings.visualizationStyle,
                showSprints: props.settings.showSprints,
              })
            }
          />
        </label>
      </div>
    </>
  );
}

export default Settings;
