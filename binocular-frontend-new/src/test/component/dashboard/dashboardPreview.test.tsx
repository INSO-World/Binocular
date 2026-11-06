import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import DashboardPreview from '../../../components/dashboard/dashboardPreview/dashboardPreview';
import { DashboardLayoutCategory } from '../../../types/general/dashboardLayoutType';
import type { DashboardLayout } from '../../../types/general/dashboardLayoutType';

function makeLayout(items: DashboardLayout['items'] = []): DashboardLayout {
  return { name: 'Test', category: DashboardLayoutCategory.BASIC, items };
}

function makeItem(overrides: Partial<DashboardLayout['items'][number]> = {}) {
  return { id: 1, pluginName: 'TestViz', x: 0, y: 0, width: 8, height: 4, dataPluginId: undefined, ...overrides };
}

describe('DashboardPreview', () => {
  it('C23.1 renders a container div', () => {
    const { container } = render(<DashboardPreview layout={makeLayout()} />);
    expect(container.querySelector('div')).not.toBeNull();
  });

  it('C23.2 container uses 20rem width/height by default', () => {
    const { container } = render(<DashboardPreview layout={makeLayout()} />);
    const div = container.firstChild as HTMLElement;
    expect(div.style.width).toBe('20rem');
    expect(div.style.height).toBe('20rem');
  });

  it('C23.3 container uses 15rem when small=true', () => {
    const { container } = render(<DashboardPreview layout={makeLayout()} small={true} />);
    const div = container.firstChild as HTMLElement;
    expect(div.style.width).toBe('15rem');
    expect(div.style.height).toBe('15rem');
  });

  it('C23.4 renders one child div per layout item', () => {
    const layout = makeLayout([makeItem({ id: 1 }), makeItem({ id: 2 })]);
    const { container } = render(<DashboardPreview layout={layout} />);
    const outerDiv = container.firstChild as HTMLElement;
    expect(outerDiv.children).toHaveLength(2);
  });

  it('C23.5 renders pluginName text in non-small mode', () => {
    const layout = makeLayout([makeItem({ pluginName: 'TestViz' })]);
    const { getByText } = render(<DashboardPreview layout={layout} />);
    expect(getByText('TestViz')).not.toBeNull();
  });

  it('C23.6 renders {width}x{height} in non-small mode', () => {
    const layout = makeLayout([makeItem({ width: 8, height: 4 })]);
    const { getByText } = render(<DashboardPreview layout={layout} />);
    expect(getByText('8x4')).not.toBeNull();
  });

  it('C23.7 does not render {width}x{height} in small mode', () => {
    const layout = makeLayout([makeItem({ width: 8, height: 4 })]);
    const { queryByText } = render(<DashboardPreview layout={layout} small={true} />);
    expect(queryByText('8x4')).toBeNull();
  });

  it('C23.8 empty items array renders no item divs', () => {
    const { container } = render(<DashboardPreview layout={makeLayout([])} />);
    const outerDiv = container.firstChild as HTMLElement;
    expect(outerDiv.children).toHaveLength(0);
  });
});
