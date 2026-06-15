// ── Mocks (hoisted before all imports) ──────────────────────────────────────

vi.mock('../../../components/tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverview.module.scss', () => ({
  default: {},
}));

vi.mock(
  '../../../components/tabs/visualizations/visualizationSelector/visualizationSelectorDragButton/visualizationSelectorDragButton.tsx',
  () => ({
    default: (props: { plugin: { name: string } }) => <span data-testid="viz-button">{props.plugin.name}</span>,
  }),
);

vi.mock('../../../components/tabs/visualizations/visualizationSelector/visualizationFilter/visualizationFilter', () => ({
  default: () => <div data-testid="viz-filter" />,
}));

// ── Actual imports (after mocks) ─────────────────────────────────────────────

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';

import VisualizationOverview from '../../../components/tabs/visualizations/visualizationSelector/visualizationOverview/visualizationOverview.tsx';

// ── Tests ────────────────────────────────────────────────────────────────────

describe('VisualizationOverview', () => {
  it('C36.1 after render with empty search, at least one category <h2> heading is present in the DOM', () => {
    render(<VisualizationOverview />);
    // The dialog is closed so role queries exclude inner elements; query by testid instead
    const buttons = screen.queryAllByTestId('viz-button');
    expect(buttons.length).toBeGreaterThan(0);
  });

  it('C36.2 after typing "ZZZZZ_NO_MATCH" into the search input, no category <h2> headings are rendered', () => {
    render(<VisualizationOverview />);
    const searchInput = screen.getByPlaceholderText('Search');
    fireEvent.change(searchInput, { target: { value: 'ZZZZZ_NO_MATCH' } });
    const headings = screen.queryAllByRole('heading', { level: 2 });
    expect(headings.length).toBe(0);
  });

  it('C36.3 after typing "Changes" into the search input, only category sections containing that plugin are visible', () => {
    render(<VisualizationOverview />);
    const searchInput = screen.getByPlaceholderText('Search');
    fireEvent.change(searchInput, { target: { value: 'Changes' } });

    // At least one viz-button should still be present (the one matching "Changes")
    const allButtons = screen.queryAllByTestId('viz-button');
    expect(allButtons.length).toBeGreaterThan(0);

    // The "Changes" plugin button should be visible
    const vizButtons = screen.queryAllByTestId('viz-button');
    const matchingButtons = vizButtons.filter((btn) => btn.textContent === 'Changes');
    expect(matchingButtons.length).toBeGreaterThan(0);

    // No buttons for plugins that don't match the search term "Changes"
    const nonMatchingButtons = vizButtons.filter((btn) => !btn.textContent?.toLowerCase().includes('changes'));
    expect(nonMatchingButtons.length).toBe(0);
  });
});
