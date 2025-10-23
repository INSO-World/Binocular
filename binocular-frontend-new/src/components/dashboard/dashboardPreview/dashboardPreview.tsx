import dashboardPreviewStyles from './dashboardPreview.module.scss';
import { type DashboardLayout } from '../../../types/general/dashboardLayoutType';

function DashboardPreview(props: { layout: DashboardLayout; small?: boolean }) {
  const baseItemSize = 100 / 40;
  let baseGridSize = '20rem';
  if (props.small) {
    baseGridSize = '10rem';
  }
  return (
    <>
      <div className={dashboardPreviewStyles.dashboardPreviewContainer} style={{ width: baseGridSize, height: baseGridSize }}>
        {props.layout.items.map((item, j) => (
          <div
            key={'dashboardItem' + j}
            className={dashboardPreviewStyles.dashboardPreviewItemContainer}
            style={{
              left: baseItemSize * (item.x ? item.x : 0) + '%',
              top: baseItemSize * (item.y ? item.y : 0) + '%',
              height: baseItemSize * (item.height ? item.height : 0) + '%',
              width: baseItemSize * (item.width ? item.width : 0) + '%',
            }}>
            <div className={dashboardPreviewStyles.dashboardPreviewItem}>
              <span>{item.pluginName}</span>
              {props.small ? null : (
                <span className="text-xs">
                  {item.width}x{item.height}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

export default DashboardPreview;
