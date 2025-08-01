import type {Middleware} from 'redux';
import type {Action} from '@reduxjs/toolkit';
import { setLastAction } from '../../reducer/general/actionsReducer.ts';

const actionsMiddleware = (): Middleware => {
  return (store) => {
    return (next) => (action) => {
      if ((action as Action).type !== setLastAction.type) {
        next(action);

        // The type of action received, and its payload is not known at this point, so a ts-expect-error is necessary
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        store.dispatch(setLastAction({ action: action.type, payload: action.payload }));
      } else {
        next(action);
      }
    };
  };
};

export default actionsMiddleware;
