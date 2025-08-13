import { useEffect, useState } from 'react';
import SetupDialogStartPage from './pages/start/setupDialogStartPage.tsx';
import SetupDialogDatabasePage from './pages/database/setupDialogDatabasePage.tsx';
import SetupDialogDashboardPage from './pages/dashboard/setupDialogDashboardPage.tsx';
import SetupDialogSummaryPage from './pages/summary/setupDialogSummaryPage.tsx';

function SetupDialog() {
  const [page, setPage] = useState(1);

  const pageCount = 4;

  useEffect(() => {
    for (let i = 1; i <= pageCount; i++) {
      if (i <= page) {
        document.getElementById('setupStep' + i)?.classList.add('step-accent');
      } else {
        document.getElementById('setupStep' + i)?.classList.remove('step-accent');
      }
    }
  }, [page]);

  return (
    <dialog id={'setupDialog'} className={'modal'}>
      <div className={'modal-box max-w-full'}>
        <h3 id={'informationDialogHeadline'} className={'font-bold text-lg underline'}>
          Setup
        </h3>
        <ul className="steps steps-vertical lg:steps-horizontal w-full">
          <li data-content="●" className="step" id={'setupStep1'}>
            Start
          </li>
          <li data-content="?" className="step" id={'setupStep2'}>
            Database
          </li>
          <li data-content="?" className="step" id={'setupStep3'}>
            Dashboard
          </li>
          <li data-content="!" className="step" id={'setupStep4'}>
            Summary
          </li>
        </ul>

        {page === 1 && <SetupDialogStartPage></SetupDialogStartPage>}
        {page === 2 && <SetupDialogDatabasePage></SetupDialogDatabasePage>}
        {page === 3 && <SetupDialogDashboardPage></SetupDialogDashboardPage>}
        {page === 4 && <SetupDialogSummaryPage></SetupDialogSummaryPage>}

        <div className={'modal-action'}>
          {page > 1 && page <= pageCount && (
            <button className={'btn btn-sm btn-accent'} onClick={() => setPage(page - 1)}>
              Back
            </button>
          )}
          {page >= pageCount ? (
            <button className={'btn btn-sm btn-success'} onClick={() => location.reload()}>
              Save
            </button>
          ) : (
            <button className={'btn btn-sm btn-accent'} onClick={() => setPage(page + 1)}>
              Next
            </button>
          )}
          <form method={'dialog'}>
            <button className={'btn btn-sm btn-error'} style={{ color: '#fff' }}>
              Cancel
            </button>
          </form>
        </div>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
}

export default SetupDialog;
