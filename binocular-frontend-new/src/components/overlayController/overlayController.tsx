import InformationDialog from '../informationDialog/informationDialog.tsx';
import ExportDialog from '../exportDialog/exportDialog.tsx';
import SettingsDialog from '../settingsDialog/settingsDialog.tsx';
import NotificationController from '../notificationController/notificationController.tsx';
import EditAuthorDialog from '../tabs/authors/editAuthorDialog/editAuthorDialog.tsx';
import ContextMenu from '../contextMenu/contextMenu.tsx';
import LoadingLocalDatabaseOverlay from './overlays/loadingLocalDatabaseOverlay/loadingLocalDatabaseOverlay.tsx';
import FileTreeElementInfoDialog from '../tabs/fileTree/fileTreeElementInfoDialog/fileTreeElementInfoDialog.tsx';

function OverlayController() {
  return (
    <>
      <InformationDialog></InformationDialog>
      <ExportDialog></ExportDialog>
      <SettingsDialog></SettingsDialog>
      <NotificationController></NotificationController>
      <EditAuthorDialog></EditAuthorDialog>
      <FileTreeElementInfoDialog></FileTreeElementInfoDialog>
      <ContextMenu></ContextMenu>
      <LoadingLocalDatabaseOverlay></LoadingLocalDatabaseOverlay>
    </>
  );
}

export default OverlayController;
