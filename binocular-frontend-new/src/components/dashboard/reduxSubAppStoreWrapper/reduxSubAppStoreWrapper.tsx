import type { ReactElement } from 'react';
import { Provider } from 'react-redux';
import type { Action, Store } from '@reduxjs/toolkit';

export default function ReduxSubAppStoreWrapper(props: {
  children: ReactElement[] | ReactElement;
  store: Store<unknown, Action, unknown>;
}) {
  return (
    <>
      <Provider store={props.store}>{props.children}</Provider>
    </>
  );
}
