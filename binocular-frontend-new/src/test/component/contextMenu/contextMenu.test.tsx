import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import ContextMenu from '../../../components/contextMenu/contextMenu';

beforeEach(() => {
  HTMLDialogElement.prototype.close = vi.fn();
});

describe('ContextMenu', () => {
  it('C18.1 renders a <dialog> element', () => {
    render(<ContextMenu />);
    expect(document.querySelector('dialog')).not.toBeNull();
  });

  it('C18.2 dialog has id="contextMenu"', () => {
    render(<ContextMenu />);
    expect(document.getElementById('contextMenu')).not.toBeNull();
  });

  it('C18.3 contains div with id="contextMenuPositionController"', () => {
    render(<ContextMenu />);
    expect(document.getElementById('contextMenuPositionController')).not.toBeNull();
  });

  it('C18.4 contains ul with id="contextMenuContent"', () => {
    render(<ContextMenu />);
    expect(document.getElementById('contextMenuContent')).not.toBeNull();
  });

  it('C18.5 clicking the dialog calls .close() on it', () => {
    render(<ContextMenu />);
    const dialog = document.getElementById('contextMenu') as HTMLDialogElement;
    fireEvent.click(dialog);
    expect(dialog.close).toHaveBeenCalled();
  });
});
