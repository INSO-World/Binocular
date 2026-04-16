import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import DateRange from '../../../components/tabs/parameters/dataRange/dateRange.tsx';
import type { ParametersDateRangeType } from '../../../types/parameters/parametersDateRangeType.ts';

const defaultDateRange: ParametersDateRangeType = {
  from: '2024-01-15T10:30:00.000Z',
  to: '2024-06-15T10:30:00.000Z',
};

describe('DateRange', () => {
  let setParametersDateRange: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    setParametersDateRange = vi.fn();
  });

  afterEach(() => {
    // Ensure shift key is released between tests
    fireEvent.keyUp(window, { key: 'Shift' });
  });

  it('C30.1 renders two datetime-local inputs (from and to)', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const inputs = screen.getAllByDisplayValue(/\d{4}-\d{2}-\d{2}/);
    expect(inputs.length).toBeGreaterThanOrEqual(2);
    inputs.forEach((input) => {
      expect((input as HTMLInputElement).type).toBe('datetime-local');
    });
  });

  it('C30.2 from input value reflects parametersDateRange.from prop', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    // The component sets value={from.split('.')[0]} — strips milliseconds and timezone.
    // jsdom may normalise '2024-01-15T10:30:00' → '2024-01-15T10:30', so match by date prefix.
    const inputs = screen.getAllByDisplayValue(/2024-01-15/);
    expect(inputs.length).toBeGreaterThanOrEqual(1);
    expect((inputs[0] as HTMLInputElement).type).toBe('datetime-local');
  });

  it('C30.3 to input value reflects parametersDateRange.to prop', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    // The component sets value={to.split('.')[0]} — strips milliseconds and timezone.
    // jsdom may normalise '2024-06-15T10:30:00' → '2024-06-15T10:30', so match by date prefix.
    const inputs = screen.getAllByDisplayValue(/2024-06-15/);
    expect(inputs.length).toBeGreaterThanOrEqual(1);
    expect((inputs[0] as HTMLInputElement).type).toBe('datetime-local');
  });

  it('C30.4 changing from input calls setParametersDateRange with updated from', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const newFromValue = '2023-03-10T08:00';
    const inputs = screen.getAllByDisplayValue(/\d{4}-\d{2}-\d{2}/);
    // The "from" input is the first datetime-local input
    fireEvent.change(inputs[0], { target: { value: newFromValue } });
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    expect(callArg.from).toBe(newFromValue);
    expect(callArg.to).toBe(defaultDateRange.to);
  });

  it('C30.5 changing to input calls setParametersDateRange with updated to', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const newToValue = '2025-12-31T23:59';
    const inputs = screen.getAllByDisplayValue(/\d{4}-\d{2}-\d{2}/);
    // The "to" input is the second datetime-local input
    fireEvent.change(inputs[1], { target: { value: newToValue } });
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    expect(callArg.from).toBe(defaultDateRange.from);
    expect(callArg.to).toBe(newToValue);
  });

  it('C30.6 clicking "-M" button (no shift) calls setParametersDateRange with from/to ~1 month earlier', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    // Two "-M" buttons: first affects "from", second affects "to"
    const minusMonthButtons = screen.getAllByText('-M');
    expect(minusMonthButtons.length).toBe(2);

    // Click the first "-M" (affects "from")
    fireEvent.click(minusMonthButtons[0]);
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    // from should be ~1 month earlier than 2024-01-15
    expect(callArg.from).toContain('2023-12');
    expect(callArg.to).toBe(defaultDateRange.to);

    // Click the second "-M" (affects "to")
    setParametersDateRange.mockClear();
    fireEvent.click(minusMonthButtons[1]);
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArgTo = setParametersDateRange.mock.calls[0][0];
    expect(callArgTo.from).toBe(defaultDateRange.from);
    expect(callArgTo.to).toContain('2024-05');
  });

  it('C30.7 clicking "+M" button (no shift) calls setParametersDateRange with from/to ~1 month later', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const plusMonthButtons = screen.getAllByText('+M');
    expect(plusMonthButtons.length).toBe(2);

    // Click the first "+M" (affects "from")
    fireEvent.click(plusMonthButtons[0]);
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    // from should be ~1 month later than 2024-01-15 → 2024-02
    expect(callArg.from).toContain('2024-02');
    expect(callArg.to).toBe(defaultDateRange.to);

    // Click the second "+M" (affects "to")
    setParametersDateRange.mockClear();
    fireEvent.click(plusMonthButtons[1]);
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArgTo = setParametersDateRange.mock.calls[0][0];
    expect(callArgTo.from).toBe(defaultDateRange.from);
    expect(callArgTo.to).toContain('2024-07');
  });

  it('C30.8 holding Shift then clicking "-M" subtracts ~1 year (not 1 month)', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);

    // Activate shift mode — the button labels change to "-Y"
    fireEvent.keyDown(window, { key: 'Shift' });
    const minusYearButtons = screen.getAllByText('-Y');
    expect(minusYearButtons.length).toBe(2);

    // Click the first "-Y" (affects "from")
    fireEvent.click(minusYearButtons[0]);
    fireEvent.keyUp(window, { key: 'Shift' });

    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    // from should be ~1 year earlier than 2024-01-15 → 2023-01
    expect(callArg.from).toContain('2023-01');
    expect(callArg.to).toBe(defaultDateRange.to);
  });

  it('C30.9 holding Shift then clicking "+M" adds ~1 year (not 1 month)', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);

    // Activate shift mode — the button labels change to "+Y"
    fireEvent.keyDown(window, { key: 'Shift' });
    const plusYearButtons = screen.getAllByText('+Y');
    expect(plusYearButtons.length).toBe(2);

    // Click the first "+Y" (affects "from")
    fireEvent.click(plusYearButtons[0]);
    fireEvent.keyUp(window, { key: 'Shift' });

    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    // from should be ~1 year later than 2024-01-15 → 2025-01
    expect(callArg.from).toContain('2025-01');
    expect(callArg.to).toBe(defaultDateRange.to);
  });

  it('C30.10 clicking "T" button calls setParametersDateRange with a value near today', () => {
    render(<DateRange disabled={false} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const todayButtons = screen.getAllByText('T');
    expect(todayButtons.length).toBe(2);

    // Click the first "T" (sets from to today)
    fireEvent.click(todayButtons[0]);
    expect(setParametersDateRange).toHaveBeenCalledOnce();
    const callArg = setParametersDateRange.mock.calls[0][0];
    // The value should start with the current year
    const currentYear = new Date().getFullYear().toString();
    expect(callArg.from).toContain(currentYear);
    expect(callArg.to).toBe(defaultDateRange.to);
  });

  it('C30.11 disabled=true makes both datetime-local inputs disabled', () => {
    render(<DateRange disabled={true} parametersDateRange={defaultDateRange} setParametersDateRange={setParametersDateRange} />);
    const inputs = screen.getAllByDisplayValue(/\d{4}-\d{2}-\d{2}/);
    expect(inputs.length).toBeGreaterThanOrEqual(2);
    inputs.forEach((input) => {
      expect(input).toBeDisabled();
    });
  });
});
