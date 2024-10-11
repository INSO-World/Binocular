//Origin: https://shav.dev/blog/socket-io-and-redux-middleware

import { io, Socket } from 'socket.io-client';

export interface SocketInterface {
  socket: Socket;
}

class SocketConnection implements SocketInterface {
  public socket: Socket;
  // The constructor will initialize the Socket Connection
  constructor() {
    this.socket = io({ path: '/wsapi' });
  }
}

let socketConnection: SocketConnection | undefined;

// The SocketFactory is responsible for creating and returning a single instance of the SocketConnection class
// Implementing the singleton pattern
class SocketFactory {
  public static create(): SocketConnection {
    if (!socketConnection) {
      socketConnection = new SocketConnection();
    }
    return socketConnection;
  }
}

export default SocketFactory;
