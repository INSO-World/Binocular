import AddSprintDialog from '../addSprintDialog/addSprintDialog.tsx';
import { type AppDispatch, useAppDispatch } from '../../../../redux';
import { sprintToEdit } from '../../../../redux/reducer/data/sprintsReducer.ts';

function AddSprint() {
  const dispatch: AppDispatch = useAppDispatch();

  return (
    <div className={'text-xs'}>
      <button
        className={'button btn btn-primary w-full'}
        onClick={() => {
          dispatch(sprintToEdit(null));
          (document.getElementById('addSprintDialog') as HTMLDialogElement).showModal();
        }}>
        Add Sprint
      </button>
      <AddSprintDialog></AddSprintDialog>
    </div>
  );
}

export default AddSprint;
