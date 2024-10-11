//Origin: https://shav.dev/blog/socket-io-and-redux-middleware

import { Middleware } from 'redux';
import SocketFactory, { SocketInterface } from './SocketFactory.ts';
import { setProgress } from '../general/progressReducer.ts';
import { ProgressType } from '../../types/general/progressType.ts';
enum SocketEvent {
  Action = 'action',
}

const socketMiddleware: Middleware = (store) => {
  let socket: SocketInterface;

  return (next) => (action) => {
    // Middleware logic for the `initSocket` action
    if (!socket && typeof window !== 'undefined') {
      // Client-side-only code
      // Create/ Get Socket Socket
      socket = SocketFactory.create();

      // Handle all price events
      socket.socket.on(SocketEvent.Action, (progress: ProgressType) => {
        store.dispatch(setProgress(progress));
      });
    }
    next(action);
  };
};

export default socketMiddleware;
