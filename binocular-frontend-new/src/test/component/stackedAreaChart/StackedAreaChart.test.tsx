import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render } from '@testing-library/react';
import { StackedAreaChart, type ChartData, type Palette } from '../../../components/stackedAreaChart/StackedAreaChart.tsx';

// Mock SVGElement methods not available in jsdom
Object.defineProperty(SVGElement.prototype, 'getBBox', {
  writable: true,
  value: vi.fn(() => ({ x: 0, y: 0, width: 100, height: 100 })),
});

const T1 = new Date('2023-01-01').getTime();
const T2 = new Date('2023-06-01').getTime();
const T3 = new Date('2023-12-31').getTime();

const sampleData: ChartData[] = [
  { date: T1, alice: 10, bob: 5 },
  { date: T2, alice: 20, bob: 3 },
  { date: T3, alice: 15, bob: 8 },
];

const samplePalette: Palette = {
  alice: { main: '#ff0000', secondary: '#cc0000' },
  bob: { main: '#00ff00', secondary: '#00cc00' },
};

const defaultProps = {
  width: 800,
  height: 400,
  data: sampleData,
  scale: [0, 30],
  palette: samplePalette,
  sprintList: [],
  settings: { visualizationStyle: 'area', showSprints: false },
};

describe('StackedAreaChart', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('C5.1 renders an SVG element', () => {
    render(<StackedAreaChart {...defaultProps} />);
    const svgElements = document.querySelectorAll('svg');
    expect(svgElements.length).toBeGreaterThan(0);
  });

  it('C5.2 SVG has the supplied width and height', () => {
    render(<StackedAreaChart {...defaultProps} width={800} height={400} />);
    const svg = document.querySelector('svg');
    expect(svg).not.toBeNull();
    expect(svg!.getAttribute('width')).toBe('800');
    expect(svg!.getAttribute('height')).toBe('400');
  });

  it('C5.3 does not throw for empty data array', () => {
    expect(() => {
      render(<StackedAreaChart {...defaultProps} data={[]} />);
    }).not.toThrow();
    const svgElements = document.querySelectorAll('svg');
    expect(svgElements.length).toBeGreaterThan(0);
  });

  it('C5.4 does not throw for empty palette', () => {
    expect(() => {
      render(<StackedAreaChart {...defaultProps} palette={{}} />);
    }).not.toThrow();
  });

  it('C5.5 renders some SVG structure (at least a <g> child element)', () => {
    render(<StackedAreaChart {...defaultProps} />);
    const gElements = document.querySelectorAll('svg g');
    expect(gElements.length).toBeGreaterThan(0);
  });

  it('C5.6 unmounts cleanly (no uncaught exceptions)', () => {
    // jsdom does not support SVG layout APIs — console.error may be called by D3
    // We only verify that unmount() does not throw an exception.
    const { unmount } = render(<StackedAreaChart {...defaultProps} />);
    expect(() => unmount()).not.toThrow();
  });
});
