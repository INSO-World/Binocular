import ConnectedDataPlugins from '../../../settingsDialog/connectedDataPlugins/connectedDataPlugins.tsx';
import DashboardLayout from './dashboardLayout/dashboardLayout.tsx';

function SetupDialogSummaryPage() {
  return (
    <>
      <h1>Summary</h1>
      <p>
        <span>This is how you configured Binocular. You can still go back if you like to change something. Press </span>
        <span className={'btn btn-success btn-xs no-animation'}>Save</span>
        <span> to save and reload binocular with the new config.</span>
      </p>
      <ConnectedDataPlugins interactable={false}></ConnectedDataPlugins>
      <DashboardLayout></DashboardLayout>
    </>
  );
}

export default SetupDialogSummaryPage;
