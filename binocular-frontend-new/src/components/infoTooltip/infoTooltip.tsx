import infoTooltipStyles from './infoTooltip.module.scss';

function InfoTooltip() {
  return (
    <dialog
      id={'infoTooltip'}
      className={infoTooltipStyles.infoTooltip}
      onMouseLeave={() => {
        (document.getElementById('infoTooltip') as HTMLDialogElement).close();
      }}
      onContextMenu={(e) => e.preventDefault()}>
      <div
        id={'infoTooltipPositionController'}
        onMouseLeave={() => {
          (document.getElementById('infoTooltip') as HTMLDialogElement).close();
        }}>
        <div id={'infoTooltipContent'}></div>
      </div>
    </dialog>
  );
}

export default InfoTooltip;
