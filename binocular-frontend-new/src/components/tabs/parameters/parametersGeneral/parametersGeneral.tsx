import type { ParametersGeneralType } from '../../../../types/parameters/parametersGeneralType.ts';

function ParametersGeneral(props: {
  disabled: boolean;
  parametersGeneral: ParametersGeneralType;
  setParametersGeneral: (parametersGeneral: ParametersGeneralType) => void;
}) {
  return (
    <div className={'text-xs'}>
      <label className="label cursor-pointer flex w-full justify-between items-center">
        <span className="label-text">Granularity:</span>
        <select
          className={'select select-bordered select-xs w-24'}
          disabled={props.disabled}
          value={props.parametersGeneral.granularity}
          onChange={(e) =>
            props.setParametersGeneral({
              granularity: e.target.value,
              excludeMergeCommits: props.parametersGeneral.excludeMergeCommits,
            })
          }>
          <option value={'years'}>Year</option>
          <option value={'months'}>Month</option>
          <option value={'weeks'}>Week</option>
          <option value={'days'}>Day</option>
        </select>
      </label>
      <label className="label cursor-pointer flex w-full justify-between items-center mt-0.5">
        <span className="label-text">Exclude Merge Commits:</span>
        <input
          type={'checkbox'}
          className={'toggle toggle-accent toggle-sm'}
          disabled={props.disabled}
          checked={props.parametersGeneral.excludeMergeCommits}
          onChange={(e) =>
            props.setParametersGeneral({ granularity: props.parametersGeneral.granularity, excludeMergeCommits: e.target.checked })
          }
        />
      </label>
    </div>
  );
}

export default ParametersGeneral;
