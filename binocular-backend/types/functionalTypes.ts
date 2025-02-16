export type Result<T> = { kind: 'ok'; value: T } | { kind: 'err'; error: Error };

export function Ok<T>(value: T): Result<T> {
  return { kind: 'ok', value };
}

export function Err<T = never>(error: Error): Result<T> {
  return { kind: 'err', error };
}
