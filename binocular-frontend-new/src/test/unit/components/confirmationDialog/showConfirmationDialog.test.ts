import { describe, it, expect, beforeEach, vi } from 'vitest';
import { showConfirmationDialog } from '../../../../components/confirmationDialog/confirmationDialog';
import type { ConfirmationDialogOptions } from '../../../../components/confirmationDialog/confirmationDialog';

const W = 1000;
const H = 800;

beforeEach(() => {
  document.body.innerHTML = `
    <dialog id="contextMenu"></dialog>
    <div id="contextMenuPositionController" style=""></div>
    <div id="contextMenuContent"></div>
  `;
  HTMLDialogElement.prototype.showModal = vi.fn();
  Object.defineProperty(window, 'innerWidth', { value: W, writable: true, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: H, writable: true, configurable: true });
});

function getContainer() {
  return document.getElementById('contextMenuPositionController') as HTMLDivElement;
}

function getContent() {
  return document.getElementById('contextMenuContent') as HTMLDivElement;
}

const noopOptions: ConfirmationDialogOptions[] = [
  { label: 'Yes', icon: null, function: vi.fn() },
  { label: 'No', icon: null, function: vi.fn() },
];

describe('showConfirmationDialog – vertical positioning', () => {
  it('U44.1 y < innerHeight/2 → top is set, bottom is auto', () => {
    showConfirmationDialog(100, 100, 350, 'Sure?', noopOptions);
    const ctrl = getContainer();
    expect(ctrl.style.top).toBe('100px');
    expect(ctrl.style.bottom).toBe('auto');
  });

  it('U44.2 y >= innerHeight/2 → bottom is set, top is auto', () => {
    showConfirmationDialog(100, 600, 350, 'Sure?', noopOptions);
    const ctrl = getContainer();
    expect(ctrl.style.bottom).toBe(`${H - 600}px`);
    expect(ctrl.style.top).toBe('auto');
  });
});

describe('showConfirmationDialog – horizontal positioning', () => {
  it('U44.3 x < innerWidth/2 → left is set, right is auto', () => {
    showConfirmationDialog(200, 100, 350, 'Sure?', noopOptions);
    const ctrl = getContainer();
    expect(ctrl.style.left).toBe('200px');
    expect(ctrl.style.right).toBe('auto');
  });

  it('U44.4 x >= innerWidth/2 → right is set, left is auto', () => {
    showConfirmationDialog(700, 100, 350, 'Sure?', noopOptions);
    const ctrl = getContainer();
    expect(ctrl.style.right).toBe(`${W - 700}px`);
    expect(ctrl.style.left).toBe('auto');
  });
});

describe('showConfirmationDialog – content rendering', () => {
  it('U44.5 displays message text in a <div>', () => {
    showConfirmationDialog(100, 100, 350, 'Are you sure?', noopOptions);
    const divs = getContent().querySelectorAll('div');
    const message = Array.from(divs).find((d) => d.textContent === 'Are you sure?');
    expect(message).toBeDefined();
  });

  it('U44.6 renders two buttons with the option labels', () => {
    const options: ConfirmationDialogOptions[] = [
      { label: 'Delete', icon: null, function: vi.fn() },
      { label: 'Cancel', icon: null, function: vi.fn() },
    ];
    showConfirmationDialog(100, 100, 350, 'Sure?', options);
    const buttons = getContent().querySelectorAll('button');
    expect(buttons).toHaveLength(2);
    expect(buttons[0].textContent).toContain('Delete');
    expect(buttons[1].textContent).toContain('Cancel');
  });

  it('U44.7 clicking option[0] button invokes its function', () => {
    const fn = vi.fn();
    const options: ConfirmationDialogOptions[] = [
      { label: 'Yes', icon: null, function: fn },
      { label: 'No', icon: null, function: vi.fn() },
    ];
    showConfirmationDialog(100, 100, 350, 'Sure?', options);
    const yesButton = getContent().querySelectorAll('button')[0] as HTMLButtonElement;
    yesButton.click();
    expect(fn).toHaveBeenCalledOnce();
  });

  it('U44.8 calls showModal() on the dialog', () => {
    showConfirmationDialog(100, 100, 350, 'Sure?', noopOptions);
    const dialog = document.getElementById('contextMenu') as HTMLDialogElement;
    expect(dialog.showModal).toHaveBeenCalled();
  });

  it('U44.9 adds icon <img> when option has an icon', () => {
    const options: ConfirmationDialogOptions[] = [
      { label: 'Delete', icon: 'trash.svg', function: vi.fn() },
      { label: 'Cancel', icon: null, function: vi.fn() },
    ];
    showConfirmationDialog(100, 100, 350, 'Sure?', options);
    const yesButton = getContent().querySelectorAll('button')[0];
    expect(yesButton.querySelector('img')).not.toBeNull();
  });

  it('U44.10 does not add <img> when option icon is null', () => {
    showConfirmationDialog(100, 100, 350, 'Sure?', noopOptions);
    const yesButton = getContent().querySelectorAll('button')[0];
    expect(yesButton.querySelector('img')).toBeNull();
  });
});
