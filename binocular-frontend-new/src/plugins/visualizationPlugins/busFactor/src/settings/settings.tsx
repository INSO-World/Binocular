export interface SettingsType {
  repoPath: string;
}

function Settings(props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) {
  return (
    <>
      <div>
        <label className="label flex w-full justify-between items-center mt-0.5">
          <span className="label-text">Repository path:</span>
          <input
            type="text"
            className="input input-bordered input-xs w-40"
            placeholder="mein-repo"
            defaultValue={props.settings.repoPath}
            onBlur={(event) => props.setSettings({ repoPath: event.target.value })}
          />
        </label>
      </div>
    </>
  );
}

export default Settings;
