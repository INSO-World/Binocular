import { describe, it, expect } from 'vitest';
import { hash } from '../../../../../../plugins/visualizationPlugins/ownership/codeOwnership/src/utils/cryptoUtils';

const BASE64_RE = /^[A-Za-z0-9+/]+=*$/;

describe('hash', () => {
  it('U11.1 returns a non-empty base64 string', async () => {
    const result = await hash('hello');
    expect(result.length).toBeGreaterThan(0);
    expect(BASE64_RE.test(result)).toBe(true);
  });

  it('U11.2 is deterministic – same input produces the same hash', async () => {
    const a = await hash('hello');
    const b = await hash('hello');
    expect(a).toBe(b);
  });

  it('U11.3 different inputs produce different hashes', async () => {
    const a = await hash('hello');
    const b = await hash('world');
    expect(a).not.toBe(b);
  });

  it('U11.4 empty string resolves without throwing', async () => {
    const result = await hash('');
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });
});
