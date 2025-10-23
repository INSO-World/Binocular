import layoutOverviewStyles from './layoutOverview.module.scss';
import { useState } from 'react';
import { DashboardLayoutCategory } from '../../../../types/general/dashboardLayoutType';
import { recommendLayouts } from '../../../dashboard/recommendedDashboards/dashboardRegistry';
import DashboardPreview from '../../../dashboard/dashboardPreview/dashboardPreview';
import { setDashboardState } from '../../../../redux/reducer/general/dashboardReducer';
import { useSelector } from 'react-redux';
import { type AppDispatch, type RootState, useAppDispatch } from '../../../../redux';
import type { DashboardItemType } from '../../../../types/general/dashboardItemType';
import { deleteCustomLayout } from '../../../../redux/reducer/general/layoutReducer';

function LayoutOverview() {
  const dispatch: AppDispatch = useAppDispatch();
  const [search, setSearch] = useState<string>('');

  const [selectedLayout, setSelectedLayout] = useState<string>();
  const defaultDataPluginItemId = useSelector((state: RootState) => state.settings.database.defaultDataPluginItemId);

  const customLayouts = useSelector((state: RootState) => state.layout.customLayouts);

  const allLayouts = [...recommendLayouts, ...customLayouts];

  return (
    <dialog
      id={'layoutOverview'}
      className={layoutOverviewStyles.layoutOverview}
      onMouseMove={() => {
        (document.getElementById('layoutOverview') as HTMLDialogElement).close();
      }}
      onChange={(e) => e.preventDefault()}>
      <div
        id={'layoutOverviewPositionController'}
        onMouseMove={(e) => {
          e.stopPropagation();
          e.preventDefault();
        }}
        onDragLeave={() => {
          (document.getElementById('layoutOverview') as HTMLDialogElement).close();
        }}>
        <div className={layoutOverviewStyles.layoutOverviewContent}>
          <div>
            <h1>Dashboard Layouts:</h1>
            <label className="input input-bordered flex items-center gap-2 mt-2">
              <input type="text" className="grow" placeholder="Search" onChange={(e) => setSearch(e.target.value)} />
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="h-4 w-4 opacity-70">
                <path
                  fillRule="evenodd"
                  d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
                  clipRule="evenodd"
                />
              </svg>
            </label>
            {Object.values(DashboardLayoutCategory).map((category) => {
              const filteredLayouts = allLayouts.filter(
                (layout) => layout.category === category && layout.name.toLocaleLowerCase().includes(search.toLowerCase()),
              );

              if (filteredLayouts.length === 0) return null;

              return (
                <div key={category}>
                  <h2>{category}</h2>
                  <div>
                    <div className={`${layoutOverviewStyles.selectorRow} flex flex-wrap gap-1`}>
                      {filteredLayouts.map((layout, i) => {
                        return (
                          <div key={'dashboardLayout' + i} className={'card bg-base-100 w-48 shadow-xl'}>
                            <div className={'card-body p-3'}>
                              <div className={'flex justify-between'}>
                                <h2 className={'card-title'}>{layout.name}</h2>
                                {layout.category === DashboardLayoutCategory.CUSTOM && (
                                  <button
                                    className={layoutOverviewStyles.deleteButton}
                                    onClick={() => dispatch(deleteCustomLayout(layout.id))}></button>
                                )}
                              </div>
                              <DashboardPreview layout={layout} small={true}></DashboardPreview>
                              <button
                                className={'btn btn-accent w-fit'}
                                disabled={layout.name === selectedLayout}
                                onClick={() => {
                                  setSelectedLayout(layout.name);
                                  dispatch(
                                    setDashboardState(
                                      layout.items.map((item: DashboardItemType) => {
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
                  </div>
                  <div className="divider"></div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </dialog>
  );
}

export default LayoutOverview;
