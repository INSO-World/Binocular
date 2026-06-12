import { describe, it, expect } from 'vitest';
import { RuntimeException } from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/exception/RuntimeException.ts';
import { InvalidArgumentException } from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/exception/InvalidArgumentException.ts';
import { NoImplementationException } from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/exception/NoImplementationException.ts';

describe('RuntimeException', () => {
  it('U20.1 is an instanceof Error', () => {
    const e = new RuntimeException('oops', 'RuntimeException');
    expect(e).toBeInstanceOf(Error);
  });

  it('U20.2 message is set correctly', () => {
    const e = new RuntimeException('test message', 'RuntimeException');
    expect(e.message).toBe('test message');
  });

  it('U20.3 name is set to the provided value', () => {
    const e = new RuntimeException('msg', 'MyName');
    expect(e.name).toBe('MyName');
  });

  it('U20.4 code is set when provided', () => {
    const e = new RuntimeException('msg', 'RuntimeException', 42);
    expect(e.code).toBe(42);
  });

  it('U20.5 code is undefined when omitted', () => {
    const e = new RuntimeException('msg', 'RuntimeException');
    expect(e.code).toBeUndefined();
  });
});

describe('InvalidArgumentException', () => {
  it('U20.6 is instanceof RuntimeException', () => {
    const e = new InvalidArgumentException('bad arg');
    expect(e).toBeInstanceOf(RuntimeException);
  });

  it("U20.7 name === 'InvalidArgumentException'", () => {
    const e = new InvalidArgumentException('bad arg');
    expect(e.name).toBe('InvalidArgumentException');
  });

  it('U20.8 propagates code', () => {
    const e = new InvalidArgumentException('bad arg', 99);
    expect(e.code).toBe(99);
  });
});

describe('NoImplementationException', () => {
  it('U20.9 is instanceof RuntimeException', () => {
    const e = new NoImplementationException('not implemented');
    expect(e).toBeInstanceOf(RuntimeException);
  });

  it("U20.10 name === 'NoImplementationException'", () => {
    const e = new NoImplementationException('not implemented');
    expect(e.name).toBe('NoImplementationException');
  });
});
