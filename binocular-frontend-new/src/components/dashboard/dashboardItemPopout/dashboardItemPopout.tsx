import type { ReactElement } from 'react';
import { type AppDispatch, type RootState, useAppDispatch } from '../../../redux';
import { useSelector } from 'react-redux';
import PopoutController from './popoutController/popoutController.tsx';
import { addNotification } from '../../../redux/reducer/general/notificationsReducer.ts';
import { AlertType } from '../../../types/general/alertType.ts';

function DashboardItemPopout(props: { children: ReactElement; name: string; onClosing: () => void; onResize: () => void }) {
  const dispatch: AppDispatch = useAppDispatch();
  const popupCount = useSelector((state: RootState) => state.dashboard.popupCount);
  return (
    <PopoutController
      title={`Binocular #${popupCount} - ${props.name}`}
      options={{ width: 1280, height: 720 }}
      onClosing={props.onClosing}
      onError={() => dispatch(addNotification({ text: `Error opening out ${props.name} - #${popupCount}`, type: AlertType.error }))}
      onResize={props.onResize}>
      {props.children}
    </PopoutController>
  );
}

export default DashboardItemPopout;
