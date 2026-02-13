import visualizationSelectorStyles from '../visualizationSelector.module.scss';
import { addDashboardItem, placeDashboardItem } from '../../../../../redux/reducer/general/dashboardReducer.ts';
import { DragDropElementType } from '../../../../../types/general/dragDropElementType.ts';
import type { VisualizationPlugin } from '../../../../../plugins/interfaces/visualizationPlugin.ts';
import { type AppDispatch, type RootState, useAppDispatch } from '../../../../../redux';
import { useSelector } from 'react-redux';
import type { DatabaseSettingsDataPluginType } from '../../../../../types/settings/databaseSettingsType.ts';
import HelpIcon from '../../../../../assets/help_blue.svg';
import { showInfoTooltip } from '../../../../infoTooltip/infoTooltipHelper';
import InfoTooltip from '../../../../infoTooltip/infoTooltip';
import { useRef } from 'react';

function VisualizationSelectorDragButton(props: { plugin: VisualizationPlugin<unknown, unknown>; disabled: boolean; showHelp: boolean }) {
  const dispatch: AppDispatch = useAppDispatch();
  const configuredDataPlugins = useSelector((state: RootState) => state.settings.database.dataPlugins);
  const defaultDataPlugin = configuredDataPlugins.filter((dP: DatabaseSettingsDataPluginType) => dP.isDefault)[0];

  const tooltipRef = useRef<HTMLDivElement>(null);

  return (
    <>
      <InfoTooltip ref={tooltipRef}></InfoTooltip>
      <button
        draggable={!props.disabled}
        className={
          props.disabled ? visualizationSelectorStyles.disabledVisualizationButton : visualizationSelectorStyles.visualizationButton
        }
        onClick={() => {
          if (!props.disabled) {
            dispatch(
              addDashboardItem({
                id: 0,
                width: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[0] : 12,
                height: props.plugin.metadata.defaultSize ? props.plugin.metadata.defaultSize[1] : 8,
                pluginName: props.plugin.name,
                dataPluginId: defaultDataPlugin ? defaultDataPlugin.id : undefined,
              }),
            );
          }
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
                showInfoTooltip(tooltipRef, e.clientX, e.clientY, {
                  headline: props.plugin.name,
                  textContent: props.plugin.metadata.description ?? '',
                  reactContent: props.plugin.metadata.compatibility && (
                    <>
                      <h3>Compatibility</h3>
                      <div id="compatibility">
                        <div>
                          <p>
                            <b>Datatypes</b>
                          </p>
                          <p>
                            GitHub: {props.plugin.metadata.compatibility.github ? 'yes' : 'no'}
                            <br />
                            GitLab: {props.plugin.metadata.compatibility.gitlab ? 'yes' : 'no'}
                          </p>
                        </div>
                        <div>
                          <p>
                            <b>Databases</b>
                          </p>
                          <p>
                            Binocular Backend: ${props.plugin.metadata.compatibility.binocularBackend ? 'yes' : 'no'}
                            <br />
                            PouchDB: {props.plugin.metadata.compatibility.pouchDB ? 'yes' : 'no'}
                            <br />
                            Mock Data: {props.plugin.metadata.compatibility.mockData ? 'yes' : 'no'}
                            <br />
                            GitHub API: {props.plugin.metadata.compatibility.githubAPI ? 'yes' : 'no'}
                          </p>
                        </div>
                      </div>
                    </>
                  ),
                });
              }}>
              <img draggable={'false'} src={HelpIcon} alt={props.plugin.name} />
            </div>
          )}
        </div>
      </button>
    </>
  );
}

export default VisualizationSelectorDragButton;
