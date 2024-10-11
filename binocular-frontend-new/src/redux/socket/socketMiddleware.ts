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
  let throttling: boolean = false;

  return (next) => (action) => {
    // Middleware logic for the `initSocket` action
    if (!socket && typeof window !== 'undefined') {
      // Client-side-only code
      // Create/ Get Socket Socket
      socket = SocketFactory.create('ws://localhost:48763');

      // Handle all price events
      socket.socket.on(SocketEvent.Action, (progress: ProgressType) => {
        /*
         * Progress update needs to be throttled
         * because there are to many progress updates from the backend
         * which would cause huge performance issues in the frontend
         */

        if (!throttling) {
          throttling = true;

          store.dispatch(setProgress(progress));
          setTimeout(() => {
            throttling = false;
          }, 2000);
        }
      });
    }
    next(action);
  };
};

export default socketMiddleware;
