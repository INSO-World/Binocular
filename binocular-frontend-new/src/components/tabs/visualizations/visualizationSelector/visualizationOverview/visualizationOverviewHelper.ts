export function showVisualizationOverview(x: number, y: number) {
  (document.getElementById('visualizationOverview') as HTMLDialogElement).showModal();
  const visualizationOverviewWidth = (document.getElementById('visualizationOverviewPositionController') as HTMLDialogElement).offsetWidth;
  if (y >= window.innerHeight / 2) {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.top = `auto`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.bottom =
      `${window.innerHeight - y - 20}px`;
  } else {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.top = `${y - 20}px`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.bottom = `auto`;
  }
  if (x >= window.innerWidth / 2) {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.left = `auto`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.right = `${window.innerWidth - x - 20}px`;
  } else {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.left =
      `${x - visualizationOverviewWidth / 2}px`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.right = `auto`;
  }
}
