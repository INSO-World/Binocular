import { dataPlugins } from '../../../plugins/pluginRegistry.ts';
import { useSelector } from 'react-redux';
import type { RootState } from '../../../redux';
import type { DataPlugin } from '../../../plugins/interfaces/dataPlugin.ts';
import { useEffect } from 'react';
import type { DatabaseSettingsDataPluginType } from '../../../types/settings/databaseSettingsType.ts';
import DataPluginStorage from '../../../utils/dataPluginStorage.ts';
import AddDataPluginCard from '../addDataPluginCard/addDataPluginCard.tsx';
import ConnectedDataPlugins from '../connectedDataPlugins/connectedDataPlugins.tsx';

function DatabaseSettings() {
  const settingsDatabaseDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);

  useEffect(() => {
    settingsDatabaseDataPlugins.forEach((dP: DatabaseSettingsDataPluginType) => {
      DataPluginStorage.addDataPlugin(dP)
        .then(() => console.log(`${dP.name} #${dP.id} created`))
        .catch((e) => console.log(e));
    });
  }, [settingsDatabaseDataPlugins]);

  return (
    <>
      <div className={'card border border-base-300 shadow-md mt-1'}>
        <div className={'card-body'}>
          <ConnectedDataPlugins interactable={true}></ConnectedDataPlugins>
          <h2 className={'font-bold'}>Add Database Connection:</h2>
          <div className={'flex overflow-x-auto'}>
            {dataPlugins
              .map((dataPlugin) => new dataPlugin())
              .map((dataPlugin: DataPlugin) => (
                <AddDataPluginCard key={dataPlugin.name} dataPlugin={dataPlugin}></AddDataPluginCard>
              ))}
          </div>
        </div>
      </div>
    </>
  );
}

export default DatabaseSettings;
