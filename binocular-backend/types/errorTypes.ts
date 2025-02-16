export class TransientError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'TransientError';
  }
}

export class NonRecoverableError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'NonRecoverableError';
  }
}
