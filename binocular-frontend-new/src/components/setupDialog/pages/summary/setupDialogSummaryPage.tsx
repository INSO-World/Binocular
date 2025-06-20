import ConnectedDataPlugins from '../../../settingsDialog/connectedDataPlugins/connectedDataPlugins.tsx';
import DashboardLayout from './dashboardLayout/dashboardLayout.tsx';

function SetupDialogSummaryPage() {
  return (
    <>
      <h1>Summary</h1>
      <ConnectedDataPlugins interactable={false}></ConnectedDataPlugins>
      <DashboardLayout></DashboardLayout>
    </>
  );
}

export default SetupDialogSummaryPage;
