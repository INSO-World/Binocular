import type { VisualizationPluginCompatibility } from '../../../../../plugins/interfaces/visualizationPluginInterfaces/visualizationPluginMetadata';

export function showVisualizationOverview(x: number, y: number) {
  (document.getElementById('visualizationOverview') as HTMLDialogElement).showModal();
  const visualizationOverviewWidth = (document.getElementById('visualizationOverviewPositionController') as HTMLDialogElement).offsetWidth;
  if (y >= window.innerHeight / 2) {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.top = `auto`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.bottom =
      `${Math.min(window.innerHeight - y - 20, window.innerHeight - 10)}px`;
  } else {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.top = `${Math.max(y - 20, 10)}px`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.bottom = `auto`;
  }
  if (x >= window.innerWidth / 2) {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.left = `auto`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.right =
      `${Math.min(window.innerWidth - x - 20, window.innerWidth - 10)}px`;
  } else {
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.left =
      `${Math.max(x - visualizationOverviewWidth / 2, 10)}px`;
    (document.getElementById('visualizationOverviewPositionController') as HTMLDivElement).style.right = `auto`;
  }
}

// returns true if any boolean of filterOptions is true, while same boolean in pluginOptions is false
export function disableVisualizationOverview(
  filterOptions: VisualizationPluginCompatibility,
  pluginOptions: VisualizationPluginCompatibility | undefined,
): boolean {
  if (!pluginOptions) return false;
  let value = false;
  Object.keys(filterOptions).forEach((key) => {
    const typedKey = key as keyof VisualizationPluginCompatibility;
    if (filterOptions[typedKey] && pluginOptions && !pluginOptions[typedKey]) {
      value = true;
      return;
    }
  });
  return value;
}
