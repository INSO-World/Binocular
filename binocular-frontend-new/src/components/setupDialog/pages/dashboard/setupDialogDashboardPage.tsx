import { type AppDispatch, type RootState, useAppDispatch } from '../../../../redux';
import type { RecommendedDashboard } from '../../../../types/general/recommendedDashboardType.ts';
import defaultDashboard from './recommendedDashboards/defaultDashboard.ts';
import { setDashboardState } from '../../../../redux/reducer/general/dashboardReducer.ts';
import { useState } from 'react';
import ownershipDashboard from './recommendedDashboards/ownershipDashboard.ts';
import { useSelector } from 'react-redux';
import DashboardPreview from './dashboardPreview/dashboardPreview.tsx';

function SetupDialogDashboardPage() {
  const dispatch: AppDispatch = useAppDispatch();

  const recommendedDashboards: RecommendedDashboard[] = [defaultDashboard, ownershipDashboard];

  const [selectedDashboard, setSelectedDashboard] = useState<string>();

  const dashboardInitialized = useSelector((state: RootState) => state.dashboard.initialized);
  const defaultDataPluginItemId = useSelector((state: RootState) => state.settings.database.defaultDataPluginItemId);
  console.log(defaultDataPluginItemId);
  return (
    <>
      <h1>Setup Dashboard</h1>
      {dashboardInitialized === true ? (
        <div role="alert" className="alert alert-success">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Dashboard already Initialized! It is still possible to override it with a different default dashboard.</span>
        </div>
      ) : (
        <div>Select one of the following dashboards or press next and start with a blank dashboard.</div>
      )}
      <div className={'flex gap-4 w-full flex-wrap'}>
        {recommendedDashboards.map((recommendedDashboard, i) => {
          return (
            <div key={'recommendedDashboard' + i} className={'card bg-base-100 w-96 shadow-xl'}>
              <div className={'card-body'}>
                <h2 className={'card-title'}>{recommendedDashboard.name}</h2>
                <DashboardPreview dashboardItems={recommendedDashboard.items}></DashboardPreview>
                <button
                  className={'btn btn-accent w-fit'}
                  disabled={recommendedDashboard.name === selectedDashboard}
                  onClick={() => {
                    setSelectedDashboard(recommendedDashboard.name);
                    dispatch(
                      setDashboardState(
                        recommendedDashboard.items.map((item) => {
                          return { ...item, dataPluginId: defaultDataPluginItemId };
                        }),
                      ),
                    );
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
