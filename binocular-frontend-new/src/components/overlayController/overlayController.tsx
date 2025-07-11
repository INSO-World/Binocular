import InformationDialog from '../informationDialog/informationDialog.tsx';
import ExportDialog from '../exportDialog/exportDialog.tsx';
import SettingsDialog from '../settingsDialog/settingsDialog.tsx';
import NotificationController from '../notificationController/notificationController.tsx';
import EditAuthorDialog from '../tabs/authors/editAuthorDialog/editAuthorDialog.tsx';
import ContextMenu from '../contextMenu/contextMenu.tsx';
import LoadingLocalDatabaseOverlay from './overlays/loadingLocalDatabaseOverlay/loadingLocalDatabaseOverlay.tsx';
import FileTreeElementInfoDialog from '../tabs/fileTree/fileTreeElementInfoDialog/fileTreeElementInfoDialog.tsx';
import SetupDialog from '../setupDialog/setupDialog.tsx';
import VisualizationOverview from '../tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverview.tsx';
import InfoTooltipp from '../infoTooltipp/infoTooltipp.tsx';

function OverlayController() {
  return (
    <>
      <InformationDialog></InformationDialog>
      <ExportDialog></ExportDialog>
      <SettingsDialog></SettingsDialog>
      <SetupDialog></SetupDialog>
      <NotificationController></NotificationController>
      <EditAuthorDialog></EditAuthorDialog>
      <FileTreeElementInfoDialog></FileTreeElementInfoDialog>
      <VisualizationOverview></VisualizationOverview>
      <LoadingLocalDatabaseOverlay></LoadingLocalDatabaseOverlay>
      <ContextMenu></ContextMenu>
      <InfoTooltipp></InfoTooltipp>
    </>
  );
}

export default OverlayController;
