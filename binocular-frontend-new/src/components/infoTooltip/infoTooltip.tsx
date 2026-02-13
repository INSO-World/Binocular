import infoTooltipStyles from './infoTooltip.module.scss';
import type { RefObject } from 'react';

interface InfoTooltipProps {
  ref: RefObject<HTMLDivElement | null>;
}

function InfoTooltip({ ref }: InfoTooltipProps) {
  return (
    <div
      ref={ref}
      id={'infoTooltip'}
      className={infoTooltipStyles.infoTooltip}
      onMouseLeave={() => {
        if (ref.current) {
          ref.current.style.display = 'none';
        }
      }}
      onContextMenu={(e) => e.preventDefault()}>
      <div
        id={'infoTooltipPositionController'}
        onMouseLeave={() => {
          if (ref.current) {
            ref.current.style.display = 'none';
          }
        }}>
        <div id={'infoTooltipContent'}></div>
      </div>
    </div>
  );
}

export default InfoTooltip;
