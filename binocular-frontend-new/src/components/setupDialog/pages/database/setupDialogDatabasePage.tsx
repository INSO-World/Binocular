import { useSelector } from 'react-redux';
import { RootState } from '../../../../redux';
import { DatabaseSettingsDataPluginType } from '../../../../types/settings/databaseSettingsType.ts';

function SetupDialogDatabasePage() {
  const settingsInitialized = useSelector((state: RootState) => state.settings.initialized);
  const databaseConnections = useSelector((state: RootState) => state.settings.database.dataPlugins);
  return (
    <>
      <h1>Setup Data Connection</h1>
      {settingsInitialized === true && (
        <div role="alert" className="alert alert-info">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="h-6 w-6 shrink-0 stroke-current">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <span>Database connection already Initialized! It is still possible to add additional database connections.</span>
        </div>
      )}
      {databaseConnections.length > 0 && (
        <div>
          <h2>Already initialized data connections:</h2>
          <div className={'flex gap-4'}>
            {databaseConnections.map((databaseConnection: DatabaseSettingsDataPluginType, i: number) => {
              return (
                <div
                  key={'databaseConnection' + i}
                  className={'card bg-base-100 w-96 shadow-xl'}
                  style={{ backgroundColor: databaseConnection.color }}>
                  <div className={'card-body'}>
                    <h2 className={'card-title'}>{databaseConnection.name}</h2>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </>
  );
}

export default SetupDialogDatabasePage;
