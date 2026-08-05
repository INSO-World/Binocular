export interface SocketConnectionType {
  status: SocketConnectionStatusType;
  message?: string;
}

export enum SocketConnectionStatusType {
  'Idle',
  'Connected',
  'Disconnected',
}
