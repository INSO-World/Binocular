import { useState } from 'react';
import DatabaseSettings from './databaseSettings/databaseSettings.tsx';
import GeneralSettings from './generalSettings/generalSettings.tsx';

function SettingsDialog() {
  const [activeTab, setActiveTab] = useState('General');

  return (
    <dialog id={'settingsDialog'} className={'modal'}>
      <div className={'modal-box max-w-full h-full'}>
        <form method={'dialog'}>
          <button className="btn btn-sm btn-circle absolute right-2 top-2 btn-accent">✕</button>
        </form>
        <h3 id={'informationDialogHeadline'} className={'font-bold text-lg underline'}>
          Settings
        </h3>
        <div>
          <div role="tablist" className="tabs tabs-boxed">
            <a
              role={'tab'}
              className={'tab no-underline ' + (activeTab === 'General' ? 'tab-active' : '')}
              onClick={() => setActiveTab('General')}>
              General
            </a>
            <a
              role={'tab'}
              className={'tab no-underline ' + (activeTab === 'Database' ? 'tab-active' : '')}
              onClick={() => setActiveTab('Database')}>
              Database
            </a>
          </div>
        </div>
        {activeTab === 'General' && <GeneralSettings></GeneralSettings>}
        {activeTab === 'Database' && <DatabaseSettings></DatabaseSettings>}
      </div>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
}

export default SettingsDialog;
