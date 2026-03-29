import { describe, it, expect, beforeEach, vi } from 'vitest';
import { showDialog } from '../../../../components/informationDialog/dialogHelper';

beforeEach(() => {
  document.body.innerHTML = `
    <div id="informationDialogHeadline"></div>
    <div id="informationDialogText"></div>
    <dialog id="informationDialog"></dialog>
  `;
  HTMLDialogElement.prototype.showModal = vi.fn();
});

describe('showDialog', () => {
  it('U46.1 sets innerText of #informationDialogHeadline', () => {
    showDialog('My headline', 'My text');
    const el = document.getElementById('informationDialogHeadline') as HTMLDivElement;
    expect(el.innerText).toBe('My headline');
  });

  it('U46.2 sets innerText of #informationDialogText', () => {
    showDialog('My headline', 'My text');
    const el = document.getElementById('informationDialogText') as HTMLDivElement;
    expect(el.innerText).toBe('My text');
  });

  it('U46.3 calls showModal() on #informationDialog', () => {
    showDialog('My headline', 'My text');
    const dialog = document.getElementById('informationDialog') as HTMLDialogElement;
    expect(dialog.showModal).toHaveBeenCalledOnce();
  });
});
