//Modified version from: https://shav.dev/blog/socket-io-and-redux-middleware

import { Middleware } from 'redux';
import SocketFactory, { SocketInterface } from './SocketFactory.ts';
import { setConnectionStatus, setProgress } from '../../reducer/general/progressReducer.ts';
import { ProgressType } from '../../../types/general/progressType.ts';
import _ from 'lodash';
import { SocketConnectionStatusType } from '../../../types/general/socketConnectionType.ts';

const RETRY_INTERVAL = 10000;

const socketMiddleware = (socketURL: string): Middleware => {
  return (store) => {
    let socket: SocketInterface;
    let connected = false;

    const retryConnectOnFailure = (retryInMilliseconds: number) => {
      setTimeout(function () {
        if (!connected) {
          console.log(`Trying to reconnect to Websocket`);
          socket.socket.connect();
          retryConnectOnFailure(retryInMilliseconds);
        }
      }, retryInMilliseconds);
    };

    const throttledDispatch = _.throttle(
      (v) => {
        if (!connected) {
          store.dispatch(setConnectionStatus({ status: SocketConnectionStatusType.Connected }));
        }
        store.dispatch(v);
      },
      5000,
      { leading: true, trailing: false },
    );

    return (next) => (action) => {
      // Middleware logic for the `initSocket` action
      if (!socket && typeof window !== 'undefined') {
        // Client-side-only code
        // Create/ Get Socket Socket
        socket = SocketFactory.create(socketURL);

        socket.socket.on('connect', () => {
          connected = true;
          store.dispatch(setConnectionStatus({ status: SocketConnectionStatusType.Connected }));
        });

        // Handle all price events
        socket.socket.on('action', (progress: ProgressType) => {
          /*
           * Progress update needs to be throttled
           * because there are to many progress updates from the backend
           * which would cause huge performance issues in the frontend
           */
          throttledDispatch(setProgress(progress));
        });

        socket.socket.on('disconnect', () => {
          connected = false;
          store.dispatch(setConnectionStatus({ status: SocketConnectionStatusType.Disconnected }));
          retryConnectOnFailure(RETRY_INTERVAL);
        });
      }
      next(action);
    };
  };
};

export default socketMiddleware;
