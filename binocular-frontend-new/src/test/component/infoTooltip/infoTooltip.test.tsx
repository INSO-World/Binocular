import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import InfoTooltip from '../../../components/infoTooltip/infoTooltip';

beforeEach(() => {
  HTMLDialogElement.prototype.close = vi.fn();
});

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

  it('C19.5 onMouseLeave on the dialog calls .close() on #infoTooltip', () => {
    render(<InfoTooltip />);
    const dialog = document.getElementById('infoTooltip') as HTMLDialogElement;
    fireEvent.mouseLeave(dialog);
    expect(dialog.close).toHaveBeenCalled();
  });

  it('C19.6 onContextMenu calls e.preventDefault()', () => {
    render(<InfoTooltip />);
    const dialog = document.getElementById('infoTooltip') as HTMLDialogElement;
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    dialog.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
  });

  it('C19.7 onMouseLeave on #infoTooltipPositionController calls .close()', () => {
    render(<InfoTooltip />);
    const dialog = document.getElementById('infoTooltip') as HTMLDialogElement;
    const inner = document.getElementById('infoTooltipPositionController') as HTMLElement;
    fireEvent.mouseLeave(inner);
    expect(dialog.close).toHaveBeenCalled();
  });
});
