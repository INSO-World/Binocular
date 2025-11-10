import { useSelector } from 'react-redux';
import type { RootState } from '../../../../../redux';
import DashboardPreview from '../../../../dashboard/dashboardPreview/dashboardPreview';
import { DashboardLayoutCategory } from '../../../../../types/general/dashboardLayoutType';

function DashboardLayout() {
  const dashboardItems = useSelector((root: RootState) => root.dashboard.dashboardItems);

  return (
    <>
      <h2>Dashboard Layout</h2>
      <DashboardPreview layout={{ name: '', category: DashboardLayoutCategory.BASIC, items: dashboardItems }}></DashboardPreview>
    </>
  );
}

export default DashboardLayout;
