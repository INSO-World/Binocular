import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import HatchPattern from '../../../components/svg/patterns/hatch';

describe('HatchPattern', () => {
  it('C16.1 renders a <pattern> element', () => {
    const { container } = render(
      <svg>
        <HatchPattern id="hatch" color="blue" />
      </svg>,
    );
    expect(container.querySelector('pattern')).not.toBeNull();
  });

  it('C16.2 <pattern> has the supplied id', () => {
    const { container } = render(
      <svg>
        <HatchPattern id="myHatch" color="blue" />
      </svg>,
    );
    expect(container.querySelector('pattern')!.id).toBe('myHatch');
  });

  it('C16.3 <path> style has the supplied color as stroke', () => {
    const { container } = render(
      <svg>
        <HatchPattern id="hatch" color="blue" />
      </svg>,
    );
    const path = container.querySelector('path') as SVGPathElement;
    expect(path).not.toBeNull();
    expect((path as unknown as HTMLElement).style.stroke).toBe('blue');
  });

  it('C16.4 <pattern> has width="4" and height="4"', () => {
    const { container } = render(
      <svg>
        <HatchPattern id="hatch" color="blue" />
      </svg>,
    );
    const pattern = container.querySelector('pattern')!;
    expect(pattern.getAttribute('width')).toBe('4');
    expect(pattern.getAttribute('height')).toBe('4');
  });
});
