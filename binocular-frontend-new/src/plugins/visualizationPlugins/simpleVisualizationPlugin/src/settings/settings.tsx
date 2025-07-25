export interface DefaultSettings {
  visualizationStyle: string;
  showSprints: boolean;
}

export interface BuildSettings extends DefaultSettings {
  splitBuildsPerAuthor: boolean;
}

export interface ChangesSettings extends DefaultSettings {
  splitAdditionsDeletions: boolean;
}

export interface TimeSpentSettings extends DefaultSettings {
  splitByIssue: boolean;
  splitSpentRemoved: boolean;
}

// TODO this function is currently ignored, generalization should be done for the basic settings as well
function Settings(props: { settings: DefaultSettings; setSettings: (newSettings: DefaultSettings) => void }) {
  return (
    <>
      <div>
        <label className="form-control w-full max-w-xs">
          <div className="label">
            <span className="label-text">Visualization Style:</span>
          </div>
          <select
            className="select select-bordered select-sm"
            defaultValue={props.settings.visualizationStyle}
            onChange={(e) =>
              props.setSettings({
                visualizationStyle: e.target.value,
                showSprints: props.settings.showSprints,
              })
            }>
            <option value={'curved'}>curved</option>
            <option value={'stepped'}>stepped</option>
            <option value={'linear'}>linear</option>
          </select>
        </label>
        <label className="label cursor-pointer">
          <span className="label-text">Show Sprints:</span>
          <input
            type="checkbox"
            className="toggle toggle-accent toggle-sm"
            defaultChecked={props.settings.showSprints}
            onChange={(event) =>
              props.setSettings({
                visualizationStyle: props.settings.visualizationStyle,
                showSprints: event.target.checked,
              })
            }
          />
        </label>
      </div>
    </>
  );
}

export default Settings;
