import { visualizationPlugins } from '../../../../plugins/pluginRegistry.ts';
import visualizationSelectorStyles from './visualizationSelector.module.scss';
import VisualizationsIcon from '../../../../assets/visualizations_white.svg';
import { showVisualizationOverview } from './visualizationOverview/visualizationOverviewHelper.ts';
import VisualizationSelectorDragButton from './visualizationSelectorDragButton/visualizationSelectorDragButton.tsx';
function VisualizationSelector(props: { orientation?: string }) {
  return (
    <div className={'text-xs'}>
      <div
        className={
          visualizationSelectorStyles.selector +
          ' ' +
          (props.orientation === 'horizontal'
            ? visualizationSelectorStyles.selectorHorizontal
            : visualizationSelectorStyles.selectorVertical)
        }>
        <div className={visualizationSelectorStyles.selectorRow}>
          {visualizationPlugins
            .filter((plugin) => plugin.metadata.recommended === true)
            .map((plugin, i) => {
              return (
                <VisualizationSelectorDragButton
                  key={'VisualizationSelectorV' + i}
                  plugin={plugin}
                  showHelp={false}></VisualizationSelectorDragButton>
              );
            })}
        </div>
        <button className="btn btn-square btn-accent btn-sm" onClick={(e) => showVisualizationOverview(e.clientX, e.clientY)}>
          <img src={VisualizationsIcon} />
        </button>
      </div>
    </div>
  );
}

export default VisualizationSelector;
