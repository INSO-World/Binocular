import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import InfoTooltip from '../../../components/infoTooltip/infoTooltip';

let ref: { current: HTMLDivElement | null };
let tooltipVisibleFlagRef: { current: boolean };

beforeEach(() => {
  ref = { current: null };
  tooltipVisibleFlagRef = { current: false };
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe('InfoTooltip', () => {
  it('C19.1 renders a div with id="infoTooltip"', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    expect(document.getElementById('infoTooltip')).not.toBeNull();
  });

  it('C19.2 div has id="infoTooltip"', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    expect(document.getElementById('infoTooltip')).not.toBeNull();
  });

  it('C19.3 contains div with id="infoTooltipPositionController"', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    expect(document.getElementById('infoTooltipPositionController')).not.toBeNull();
  });

  it('C19.4 contains div with id="infoTooltipContent"', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    expect(document.getElementById('infoTooltipContent')).not.toBeNull();
  });

  it('C19.5 onMouseLeave on the infoTooltip div hides it after timeout', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    const container = document.getElementById('infoTooltip') as HTMLDivElement;
    ref.current = container;
    container.style.display = 'block';

    fireEvent.mouseLeave(container);
    vi.advanceTimersByTime(600);

    expect(container.style.display).toBe('none');
  });

  it('C19.6 onContextMenu calls e.preventDefault()', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    const container = document.getElementById('infoTooltip') as HTMLDivElement;
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    container.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
  });

  it('C19.7 onMouseLeave on infoTooltip sets tooltipVisibleFlagRef to false', () => {
    render(<InfoTooltip ref={ref} tooltipVisibleFlagRef={tooltipVisibleFlagRef} />);
    const container = document.getElementById('infoTooltip') as HTMLDivElement;
    ref.current = container;
    tooltipVisibleFlagRef.current = true;

    fireEvent.mouseLeave(container);

    expect(tooltipVisibleFlagRef.current).toBe(false);
  });
});
