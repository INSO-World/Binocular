import visualizationSelectorStyles from '../visualizationSelector.module.scss';
import { addDashboardItem, placeDashboardItem } from '../../../../../redux/reducer/general/dashboardReducer.ts';
import { DragDropElementType } from '../../../../../types/general/dragDropElementType.ts';
import type { VisualizationPlugin } from '../../../../../plugins/interfaces/visualizationPlugin.ts';
import { type AppDispatch, type RootState, useAppDispatch } from '../../../../../redux';
import { useSelector } from 'react-redux';
import type { DatabaseSettingsDataPluginType } from '../../../../../types/settings/databaseSettingsType.ts';
import HelpIcon from '../../../../../assets/help_blue.svg';
import { showInfoTooltipp } from '../../../../infoTooltipp/infoTooltipHelper.ts';

function VisualizationSelectorDragButton(props: { plugin: VisualizationPlugin<unknown, unknown>; disabled: boolean; showHelp: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();
  const configuredDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const defaultDataPlugin = configuredDataPlugins.filter((dP: DatabaseSettingsDataPluginType) => dP.isDefault)[0];

  return (
    <button
      draggable={true}
      disabled={props.disabled}
      className={props.disabled ? visualizationSelectorStyles.disabledVisualizationButton : visualizationSelectorStyles.visualizationButton}
      onClick={() => {
        dispatch(
          addDashboardItem({
            id: 0,
            width: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[0] : 12,
            height: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[1] : 8,
            pluginName: props.plugin.name,
            dataPluginId: defaultDataPlugin ? defaultDataPlugin.id : undefined,
          }),
        );
      }}
      onDragStart={(event) => {
        event.dataTransfer.clearData();
        event.dataTransfer.setData('data', JSON.stringify({ dragDropElementType: DragDropElementType.Visualization }));
        dispatch(
          placeDashboardItem({
            id: 0,
            x: 0,
            y: 0,
            width: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[0] : 12,
            height: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[1] : 8,
            pluginName: props.plugin.name,
            dataPluginId: defaultDataPlugin ? defaultDataPlugin.id : undefined,
          }),
        );
      }}>
      <div className={'relative'}>
        <img draggable={'false'} src={props.plugin.images.thumbnail} alt={props.plugin.name} />
        <span>{props.plugin.name}</span>
        {props.plugin.metadata.description && props.showHelp && (
          <div
            className={visualizationSelectorStyles.visualizationHelpButton}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              showInfoTooltipp(
                e.clientX,
                e.clientY,
                { headline: props.plugin.name, text: props.plugin.metadata.description ?? '' },
                props.plugin.metadata.compatibility,
              );
            }}>
            <img draggable={'false'} src={HelpIcon} alt={props.plugin.name} />
          </div>
        )}
      </div>
    </button>
  );
}

export default VisualizationSelectorDragButton;
