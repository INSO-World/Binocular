import setupDialogDashboardPageStyles from './setupDialogDashboardPage.module.scss';
import { AppDispatch, RootState, useAppDispatch } from '../../../../redux';
import { RecommendedDashboard } from '../../../../types/general/recommendedDashboardType.ts';
import defaultDashboard from './recommendedDashboards/defaultDashboard.ts';
import { setDashboardState } from '../../../../redux/reducer/general/dashboardReducer.ts';
import { useState } from 'react';
import ownershipDashboard from './recommendedDashboards/ownershipDashboard.ts';
import { useSelector } from 'react-redux';

function SetupDialogDashboardPage() {
  const dispatch: AppDispatch = useAppDispatch();

  const recommendedDashboards: RecommendedDashboard[] = [defaultDashboard, ownershipDashboard];

  const [selectedDashboard, setSelectedDashboard] = useState<string>();

  const dashboardInitialized = useSelector((state: RootState) => state.dashboard.initialized);
  return (
    <>
      <h1>Setup Dashboard</h1>
      {dashboardInitialized === true ? (
        <div role="alert" className="alert alert-info">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="h-6 w-6 shrink-0 stroke-current">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <span>Dashboard already Initialized! It is still possible to override it with a different default dashboard.</span>
        </div>
      ) : (
        <div>Select one of the following dashboards or press next and start with a blank dashboard.</div>
      )}
      <div className={setupDialogDashboardPageStyles.dashboardSelector}>
        {recommendedDashboards.map((recommendedDashboard, i) => {
          return (
            <div key={'recommendedDashbaord' + i} className={'card bg-base-100 w-96 shadow-xl'}>
              <div className={'card-body'}>
                <h2 className={'card-title'}>{recommendedDashboard.name}</h2>
                <div className={setupDialogDashboardPageStyles.dashboardPreviewContainer}>
                  {recommendedDashboard.items.map((item, j) => (
                    <div
                      key={'recommendedDashbaord' + i + 'Item' + j}
                      className={setupDialogDashboardPageStyles.dashboardPreviewItemContainer}
                      style={{
                        left: (100 / 40) * (item.x ? item.x : 0) + '%',
                        top: (100 / 40) * (item.y ? item.y : 0) + '%',
                        height: (100 / 40) * (item.height ? item.height : 0) + '%',
                        width: (100 / 40) * (item.width ? item.width : 0) + '%',
                      }}>
                      <div className={setupDialogDashboardPageStyles.dashboardPreviewItem}>
                        <span>{item.pluginName}</span>
                        <span className={'text-xs'}>
                          {item.width}x{item.height}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
                <button
                  className={'btn btn-accent w-fit'}
                  disabled={recommendedDashboard.name === selectedDashboard}
                  onClick={() => {
                    setSelectedDashboard(recommendedDashboard.name);
                    dispatch(setDashboardState(recommendedDashboard.items));
                  }}>
                  Select
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}

export default SetupDialogDashboardPage;
