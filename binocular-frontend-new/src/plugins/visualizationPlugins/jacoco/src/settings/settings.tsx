export interface SettingsType {
  selectedReport: string;
}

function Settings(props: { settings: SettingsType; setSettings: (newSettings: SettingsType) => void }) {
  return (
    <div className={'text-xs'}>
      <table>
        <tbody>
          <tr>
            <td>Report:</td>
            <td>
              <select
                className={'select select-accent select-xs'}
                defaultValue={props.settings.selectedReport}
                onChange={(event) =>
                  props.setSettings({
                    selectedReport: event.target.value,
                  })
                }>
                <option value={'last'}>last</option>
                <option value={'first'}>first</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

export default Settings;
