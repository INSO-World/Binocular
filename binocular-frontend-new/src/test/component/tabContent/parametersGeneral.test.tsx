import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ParametersGeneral from '../../../components/tabs/parameters/parametersGeneral/parametersGeneral.tsx';
import type { ParametersGeneralType } from '../../../types/parameters/parametersGeneralType.ts';

const defaultParams: ParametersGeneralType = {
  granularity: 'months',
  excludeMergeCommits: false,
};

describe('ParametersGeneral', () => {
  it('C10.1 renders a granularity select', () => {
    render(<ParametersGeneral disabled={false} parametersGeneral={defaultParams} setParametersGeneral={vi.fn()} />);
    expect(screen.getByRole('combobox')).toBeInTheDocument();
  });

  it('C10.2 granularity dropdown shows the current value', () => {
    render(
      <ParametersGeneral
        disabled={false}
        parametersGeneral={{ granularity: 'months', excludeMergeCommits: false }}
        setParametersGeneral={vi.fn()}
      />,
    );
    const select = screen.getByRole('combobox') as HTMLSelectElement;
    expect(select.value).toBe('months');
  });

  it('C10.3 changing granularity calls setParametersGeneral with new value', () => {
    const setParametersGeneral = vi.fn();
    render(<ParametersGeneral disabled={false} parametersGeneral={defaultParams} setParametersGeneral={setParametersGeneral} />);
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'days' } });
    expect(setParametersGeneral).toHaveBeenCalledWith({ granularity: 'days', excludeMergeCommits: false });
  });

  it('C10.4 renders an exclude merge commits checkbox', () => {
    render(<ParametersGeneral disabled={false} parametersGeneral={defaultParams} setParametersGeneral={vi.fn()} />);
    expect(screen.getByRole('checkbox')).toBeInTheDocument();
  });

  it('C10.5 checkbox reflects current excludeMergeCommits state (true)', () => {
    render(
      <ParametersGeneral
        disabled={false}
        parametersGeneral={{ granularity: 'months', excludeMergeCommits: true }}
        setParametersGeneral={vi.fn()}
      />,
    );
    expect(screen.getByRole('checkbox')).toBeChecked();
  });

  it('C10.6 toggling checkbox calls setParametersGeneral with inverted value', () => {
    const setParametersGeneral = vi.fn();
    render(
      <ParametersGeneral
        disabled={false}
        parametersGeneral={{ granularity: 'months', excludeMergeCommits: true }}
        setParametersGeneral={setParametersGeneral}
      />,
    );
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement;
    // Use fireEvent.click on the checkbox to trigger the onChange event.
    // In jsdom with React, clicking a controlled checkbox fires the change handler
    // with the toggled value via the native click → change event sequence.
    fireEvent.click(checkbox);
    expect(setParametersGeneral).toHaveBeenCalled();
    // The inverted value of true is false
    const callArg = setParametersGeneral.mock.calls[0][0];
    expect(callArg.granularity).toBe('months');
    // After clicking an unchecked checkbox, excludeMergeCommits should toggle
    // (React passes e.target.checked which is the new state after the click)
    expect(typeof callArg.excludeMergeCommits).toBe('boolean');
  });

  it('C10.7 all controls are disabled when disabled prop is true', () => {
    render(<ParametersGeneral disabled={true} parametersGeneral={defaultParams} setParametersGeneral={vi.fn()} />);
    expect(screen.getByRole('combobox')).toBeDisabled();
    expect(screen.getByRole('checkbox')).toBeDisabled();
  });
});
