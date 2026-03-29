import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import InfoTooltip from '../../../components/infoTooltip/infoTooltip';

describe('InfoTooltip', () => {
  it('C19.1 renders a <dialog> element', () => {
    render(<InfoTooltip />);
    expect(document.querySelector('dialog')).not.toBeNull();
  });

  it('C19.2 dialog has id="infoTooltip"', () => {
    render(<InfoTooltip />);
    expect(document.getElementById('infoTooltip')).not.toBeNull();
  });

  it('C19.3 contains div with id="infoTooltipPositionController"', () => {
    render(<InfoTooltip />);
    expect(document.getElementById('infoTooltipPositionController')).not.toBeNull();
  });

  it('C19.4 contains div with id="infoTooltipContent"', () => {
    render(<InfoTooltip />);
    expect(document.getElementById('infoTooltipContent')).not.toBeNull();
  });
});
