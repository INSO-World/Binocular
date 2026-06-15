import infoTooltipStyles from './infoTooltip.module.scss';
import type { RefObject } from 'react';
import { hideInfoTooltip } from './infoTooltipHelper';

interface InfoTooltipProps {
  ref: RefObject<HTMLDivElement | null>;
  tooltipVisibleFlagRef: RefObject<boolean>;
}

function InfoTooltip({ ref, tooltipVisibleFlagRef }: InfoTooltipProps) {
  return (
    <div
      ref={ref}
      id={'infoTooltip'}
      className={infoTooltipStyles.infoTooltip}
      onMouseEnter={() => {
        tooltipVisibleFlagRef.current = true;
      }}
      onMouseLeave={() => {
        hideInfoTooltip(ref, tooltipVisibleFlagRef);
      }}
      onContextMenu={(e) => e.preventDefault()}>
      <div id={'infoTooltipPositionController'}>
        <div id={'infoTooltipContent'}></div>
      </div>
    </div>
  );
}

export default InfoTooltip;
