import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { showInfoTooltip } from '../../../../components/infoTooltip/infoTooltipHelper';

const W = 1000;
const H = 800;

let containerDiv: HTMLDivElement;
let ref: { current: HTMLDivElement | null };
let tooltipVisibleFlagRef: { current: boolean };

beforeEach(() => {
  containerDiv = document.createElement('div');
  containerDiv.id = 'infoTooltip';
  containerDiv.style.display = 'none';

  const controller = document.createElement('div');
  controller.id = 'infoTooltipPositionController';

  const content = document.createElement('div');
  content.id = 'infoTooltipContent';

  controller.appendChild(content);
  containerDiv.appendChild(controller);
  document.body.appendChild(containerDiv);

  ref = { current: containerDiv };
  tooltipVisibleFlagRef = { current: false };

  Object.defineProperty(document.body, 'clientWidth', { value: W, configurable: true });
  Object.defineProperty(document.body, 'clientHeight', { value: H, configurable: true });
});

afterEach(() => {
  document.body.removeChild(containerDiv);
});

function getController() {
  return document.getElementById('infoTooltipPositionController') as HTMLDivElement;
}

function getContent() {
  return document.getElementById('infoTooltipContent') as HTMLDivElement;
}

const basicContent = { headline: 'Overview', textContent: 'Shows commits over time.' };

describe('showInfoTooltip – vertical positioning', () => {
  it('U32.1 y < clientHeight/2 → top set, bottom auto', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.top).toBe('90px');
    expect(ctrl.style.bottom).toBe('auto');
  });

  it('U32.2 y >= clientHeight/2 → bottom set, top auto', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 600, basicContent);
    const ctrl = getController();
    expect(ctrl.style.bottom).toBe(`${H - 600 - 10}px`);
    expect(ctrl.style.top).toBe('auto');
  });
});

describe('showInfoTooltip – horizontal positioning', () => {
  it('U32.3 x < clientWidth/2 → left set, right auto', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 200, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.left).toBe('190px');
    expect(ctrl.style.right).toBe('auto');
  });

  it('U32.4 x >= clientWidth/2 → right set, left auto', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 700, 100, basicContent);
    const ctrl = getController();
    expect(ctrl.style.right).toBe(`${W - 700 - 10}px`);
    expect(ctrl.style.left).toBe('auto');
  });
});

describe('showInfoTooltip – content rendering', () => {
  it('U32.5 renders <h1> with correct headline', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent);
    const h1 = getContent().querySelector('h1') as HTMLHeadingElement;
    expect(h1).not.toBeNull();
    expect(h1.innerText).toBe('Overview');
  });

  it('U32.6 renders <p> with correct body text', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent);
    const p = getContent().querySelector('p') as HTMLParagraphElement;
    expect(p).not.toBeNull();
    expect(p.innerText).toBe('Shows commits over time.');
  });

  it('U32.7 no <p> element when textContent is omitted', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, { headline: 'Title only' });
    expect(getContent().querySelector('p')).toBeNull();
  });

  it('U32.8 sets tooltipVisibleFlagRef.current to true', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent);
    expect(tooltipVisibleFlagRef.current).toBe(true);
  });

  it('U32.9 sets style.display to "block"', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent);
    expect(ref.current!.style.display).toBe('block');
  });

  it('U32.10 clears previous content before rendering new content', () => {
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, { headline: 'First' });
    showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, { headline: 'Second' });
    const headings = getContent().querySelectorAll('h1');
    expect(headings.length).toBe(1);
    expect((headings[0] as HTMLHeadingElement).innerText).toBe('Second');
  });

  it('U32.11 returns early when ref.current is null', () => {
    ref.current = null;
    expect(() => showInfoTooltip(ref, tooltipVisibleFlagRef, 100, 100, basicContent)).not.toThrow();
    expect(tooltipVisibleFlagRef.current).toBe(false);
  });
});
