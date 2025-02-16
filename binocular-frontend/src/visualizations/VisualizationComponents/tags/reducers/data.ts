import { Action, handleActions } from 'redux-actions';
import { Commit } from '../../../../types/commitTypes.ts';

interface DataState {
  commits: Commit[];
  totalCount: number;
  isFetching: boolean;
}

const initialState: DataState = {
  commits: [],
  totalCount: 0,
  isFetching: false,
};

export default handleActions(
  {
    RECEIVE_TAGS_DATA: (state, action: Action<any>) => {
      return {
        ...state,
        commits: action.payload.commits,
        totalCount: action.payload.totalCount,
        isFetching: false,
      };
    },
    REQUEST_TAGS_DATA: (state) => {
      return {
        ...state,
        isFetching: true,
      };
    },
    RECEIVE_TAGS_DATA_ERROR: (state) => {
      return {
        ...state,
        isFetching: false,
      };
    },
  },
  initialState,
);
