import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import DotsPattern from '../../../components/svg/patterns/dots';

describe('DotsPattern', () => {
  it('C15.1 renders a <pattern> element', () => {
    const { container } = render(
      <svg>
        <DotsPattern id="dots" color="red" />
      </svg>,
    );
    expect(container.querySelector('pattern')).not.toBeNull();
  });

  it('C15.2 <pattern> has the supplied id', () => {
    const { container } = render(
      <svg>
        <DotsPattern id="myDots" color="red" />
      </svg>,
    );
    expect(container.querySelector('pattern')!.id).toBe('myDots');
  });

  it('C15.3 <circle> has stroke set to the supplied color', () => {
    const { container } = render(
      <svg>
        <DotsPattern id="dots" color="#ff0000" />
      </svg>,
    );
    const circle = container.querySelector('circle');
    expect(circle).not.toBeNull();
    expect(circle!.getAttribute('stroke')).toBe('#ff0000');
  });

  it('C15.4 <use> elements reference the circle by correct href', () => {
    const { container } = render(
      <svg>
        <DotsPattern id="dots" color="red" />
      </svg>,
    );
    const uses = container.querySelectorAll('use');
    expect(uses.length).toBeGreaterThan(0);
    uses.forEach((use) => {
      expect(use.getAttribute('href')).toBe('#circle_dots');
    });
  });
});
