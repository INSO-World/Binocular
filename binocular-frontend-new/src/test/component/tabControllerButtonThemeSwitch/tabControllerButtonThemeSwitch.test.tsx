import { describe, it, expect, vi } from 'vitest';
import { render, fireEvent } from '@testing-library/react';
import TabControllerButtonThemeSwitch from '../../../components/tabMenu/tabControllerButtonThemeSwitch/tabControllerButtonThemeSwitch';

describe('TabControllerButtonThemeSwitch', () => {
  it('C17.1 renders a checkbox input', () => {
    const { container } = render(<TabControllerButtonThemeSwitch onChange={vi.fn()} theme="binocularDark" />);
    expect(container.querySelector('input[type="checkbox"]')).not.toBeNull();
  });

  it('C17.2 checkbox is checked when theme is binocularDark', () => {
    const { container } = render(<TabControllerButtonThemeSwitch onChange={vi.fn()} theme="binocularDark" />);
    const checkbox = container.querySelector('input[type="checkbox"]') as HTMLInputElement;
    expect(checkbox.defaultChecked).toBe(true);
  });

  it('C17.3 checkbox is unchecked when theme is binocularLight', () => {
    const { container } = render(<TabControllerButtonThemeSwitch onChange={vi.fn()} theme="binocularLight" />);
    const checkbox = container.querySelector('input[type="checkbox"]') as HTMLInputElement;
    expect(checkbox.defaultChecked).toBe(false);
  });

  it('C17.4 checking the box calls onChange with binocularDark', () => {
    const onChange = vi.fn();
    const { container } = render(<TabControllerButtonThemeSwitch onChange={onChange} theme="binocularLight" />);
    const checkbox = container.querySelector('input[type="checkbox"]') as HTMLInputElement;
    fireEvent.click(checkbox);
    expect(onChange).toHaveBeenCalledWith('binocularDark');
  });

  it('C17.5 unchecking the box calls onChange with binocularLight', () => {
    const onChange = vi.fn();
    const { container } = render(<TabControllerButtonThemeSwitch onChange={onChange} theme="binocularDark" />);
    const checkbox = container.querySelector('input[type="checkbox"]') as HTMLInputElement;
    fireEvent.click(checkbox);
    expect(onChange).toHaveBeenCalledWith('binocularLight');
  });
});
