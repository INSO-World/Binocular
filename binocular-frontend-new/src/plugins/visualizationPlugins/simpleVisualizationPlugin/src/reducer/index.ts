import { createSlice, Draft, PayloadAction } from '@reduxjs/toolkit';

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

//currently not used
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

//TODO: this is not a good way to do this, but it works for now
export function getDataSlice<DataType>(name: string) {
  const initialState: State<DataType> = {
    data: [],
    dateRange: { from: new Date().toISOString(), to: new Date().toISOString() },
    dataState: DataState.EMPTY,
  };
  return createSlice({
    name: name.toLowerCase(),
    initialState,
    reducers: {
      setData: (state, action: PayloadAction<DataType[]>) => {
        state.data = action.payload as Draft<DataType>[];
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

//export const { setData, setDateRange, setDataState } = dataSlice.actions;
export default dataSlice.reducer;
