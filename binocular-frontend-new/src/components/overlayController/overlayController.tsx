import InformationDialog from '../informationDialog/informationDialog.tsx';
import ExportDialog from '../exportDialog/exportDialog.tsx';
import SettingsDialog from '../settingsDialog/settingsDialog.tsx';
import NotificationController from '../notificationController/notificationController.tsx';
import EditAuthorDialog from '../tabs/authors/editAuthorDialog/editAuthorDialog.tsx';
import ContextMenu from '../contextMenu/contextMenu.tsx';
import LoadingLocalDatabaseOverlay from './overlays/loadingLocalDatabaseOverlay/loadingLocalDatabaseOverlay.tsx';
import SetupDialog from '../setupDialog/setupDialog.tsx';
import VisualizationOverview from '../tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverview.tsx';
import LayoutOverview from '../tabs/layouts/layoutOverview/layoutOverview';
import FileTreeElementInfoDialog from '../fileTree/fileTreeElementInfoDialog/fileTreeElementInfoDialog';

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
      <LayoutOverview></LayoutOverview>
      <LoadingLocalDatabaseOverlay></LoadingLocalDatabaseOverlay>
      <ContextMenu></ContextMenu>
    </>
  );
}

export default OverlayController;
