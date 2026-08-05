import { useSelector } from 'react-redux';
import type { RootState } from '../../../../redux';
import { LocalDatabaseLoadingState } from '../../../../redux/reducer/settings/settingsReducer.ts';

function LoadingLocalDatabaseOverlay() {
  const localDatabaseLoadingState = useSelector((state: RootState) => state.settings.localDatabaseLoadingState);
  const localDatabaseLoadingMessage = useSelector((state: RootState) => state.settings.localDatabaseLoadingMessage);
  return (
    <>
      {localDatabaseLoadingState === LocalDatabaseLoadingState.loading && (
        <dialog id="my_modal_1" className="modal" open={true}>
          <div className="modal-box">
            <h3 className="font-bold text-lg">
              Loading Local Database <span className="loading loading-spinner loading-lg text-accent"></span>
            </h3>
            <p className="py-4">
              <progress
                className="progress progress-accent w-full"
                value={localDatabaseLoadingMessage.split('/')[0]}
                max={localDatabaseLoadingMessage.includes('/') ? parseInt(localDatabaseLoadingMessage.split('/')[1]) : 0}></progress>
              <span>{localDatabaseLoadingMessage}</span>
            </p>
          </div>
        </dialog>
      )}
    </>
  );
}

export default LoadingLocalDatabaseOverlay;
