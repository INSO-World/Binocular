export function showLayoutOverview(x: number, y: number) {
  (document.getElementById('layoutOverview') as HTMLDialogElement).showModal();
  const layoutOverviewWidth = (document.getElementById('layoutOverviewPositionController') as HTMLDialogElement).offsetWidth;
  if (y >= window.innerHeight / 2) {
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.top = `auto`;
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.bottom =
      `${Math.min(window.innerHeight - y - 20, window.innerHeight - 10)}px`;
  } else {
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.top = `${Math.max(y - 20, 10)}px`;
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.bottom = `auto`;
  }
  if (x >= window.innerWidth / 2) {
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.left = `auto`;
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.right =
      `${Math.min(window.innerWidth - x - 20, window.innerWidth - 10)}px`;
  } else {
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.left =
      `${Math.max(x - layoutOverviewWidth / 2, 10)}px`;
    (document.getElementById('layoutOverviewPositionController') as HTMLDivElement).style.right = `auto`;
  }
}
