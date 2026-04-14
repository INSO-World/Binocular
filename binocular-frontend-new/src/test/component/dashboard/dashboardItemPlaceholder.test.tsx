import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import DashboardItemPlaceholder from '../../../components/dashboard/dashboardItemPlaceholder/dashboardItemPlaceholder';
import type { DashboardItemType } from '../../../types/general/dashboardItemType';

function makeItem(overrides: Partial<DashboardItemType> = {}): DashboardItemType {
  return { id: 3, pluginName: 'Chart', x: 1, y: 2, width: 6, height: 3, dataPluginId: undefined, ...overrides };
}

describe('DashboardItemPlaceholder', () => {
  it('C24.1 renders div with id="dashboardItem{id}"', () => {
    render(<DashboardItemPlaceholder item={makeItem({ id: 5 })} cellSize={50} colCount={4} rowCount={4} />);
    expect(document.getElementById('dashboardItem5')).not.toBeNull();
  });

  it('C24.2 label span contains "{pluginName} #{id}"', () => {
    const { getByText } = render(
      <DashboardItemPlaceholder item={makeItem({ pluginName: 'Chart', id: 3 })} cellSize={50} colCount={4} rowCount={4} />,
    );
    expect(getByText('Chart #3')).not.toBeNull();
  });

  it('C24.3 top style is a calc(...) string', () => {
    render(<DashboardItemPlaceholder item={makeItem()} cellSize={50} colCount={4} rowCount={4} />);
    const div = document.getElementById('dashboardItem3') as HTMLElement;
    expect(div.style.top).toMatch(/^calc\(/);
  });

  it('C24.4 left style is a calc(...) string', () => {
    render(<DashboardItemPlaceholder item={makeItem()} cellSize={50} colCount={4} rowCount={4} />);
    const div = document.getElementById('dashboardItem3') as HTMLElement;
    expect(div.style.left).toMatch(/^calc\(/);
  });

  it('C24.5 top contains correct percentage for rowCount=4, y=2 → 50%', () => {
    render(<DashboardItemPlaceholder item={makeItem({ id: 10, y: 2 })} cellSize={50} colCount={4} rowCount={4} />);
    const div = document.getElementById('dashboardItem10') as HTMLElement;
    // (100 / 4) * 2 = 50%
    expect(div.style.top).toContain('50%');
  });

  it('C24.6 left contains correct percentage for colCount=5, x=1 → 20%', () => {
    render(<DashboardItemPlaceholder item={makeItem({ id: 11, x: 1 })} cellSize={50} colCount={5} rowCount={4} />);
    const div = document.getElementById('dashboardItem11') as HTMLElement;
    // (100 / 5) * 1 = 20%
    expect(div.style.left).toContain('20%');
  });
});
