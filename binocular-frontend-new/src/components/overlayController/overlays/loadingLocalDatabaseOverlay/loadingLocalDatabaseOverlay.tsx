import { useSelector } from 'react-redux';
import { RootState } from '../../../../redux';
import { LocalDatabaseLoadingState } from '../../../../redux/reducer/settings/settingsReducer.ts';

function LoadingLocalDatabaseOverlay() {
  const localDatabaseLoadingState = useSelector((state: RootState) => state.settings.localDatabaseLoadingState);
  return (
    <>
      {localDatabaseLoadingState === LocalDatabaseLoadingState.loading && (
        <dialog id="my_modal_1" className="modal" open={true}>
          <div className="modal-box">
            <h3 className="font-bold text-lg">Loading Local Database</h3>
            <p className="py-4">
              <progress className="progress progress-accent w-full"></progress>
            </p>
          </div>
        </dialog>
      )}
    </>
  );
}

export default LoadingLocalDatabaseOverlay;
