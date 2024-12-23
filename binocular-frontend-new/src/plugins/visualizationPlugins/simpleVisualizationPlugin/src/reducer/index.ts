import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export enum DataState {
  EMPTY,
  FETCHING,
  COMPLETE,
}

interface DateRange {
  from: string;
  to: string;
}

export interface State<DataType> {
  data: DataType[];
  dateRange: DateRange;
  dataState: DataState;
}

const initialState: State<unknown> = {
  data: [],
  dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
  dataState: DataState.EMPTY,
};

export const dataSlice = createSlice({
  name: 'commits',
  initialState,
  reducers: {
    setData: <DataType>(state: State<DataType>, action: PayloadAction<DataType[]>) => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      state['commits'] = action.payload;
    },
    setDateRange: (state, action: PayloadAction<DateRange>) => {
      state.dateRange = action.payload;
    },
    setDataState: (state, action: PayloadAction<DataState>) => {
      state.dataState = action.payload;
    },
  },
});

export function getDataSlice<DataType>(name: string) {
  const initialState: State<DataType> = {
    data: [],
    dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
    dataState: DataState.EMPTY,
  };
  return createSlice({
    name: name,
    initialState,
    reducers: {
      setData: (state, action: PayloadAction<DataType[]>) => {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        state[name] = action.payload;
        // TODO ^ state.data?
      },
      setDateRange: (state, action: PayloadAction<DateRange>) => {
        state.dateRange = action.payload;
      },
      setDataState: (state, action: PayloadAction<DataState>) => {
        state.dataState = action.payload;
      },
    },
  });
}

export const { setData, setDateRange, setDataState } = dataSlice.actions;
export default dataSlice.reducer;
