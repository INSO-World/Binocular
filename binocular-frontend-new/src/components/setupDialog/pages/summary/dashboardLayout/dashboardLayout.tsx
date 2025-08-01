import { useSelector } from 'react-redux';
import type {RootState} from '../../../../../redux';
import DashboardPreview from '../../dashboard/dashboardPreview/dashboardPreview.tsx';

function DashboardLayout() {
  const dashboardItems = useSelector((root: RootState) => root.dashboard.dashboardItems);

  return (
    <>
      <h2>Dashboard Layout</h2>
      <DashboardPreview dashboardItems={dashboardItems}></DashboardPreview>
    </>
  );
}

export default DashboardLayout;
