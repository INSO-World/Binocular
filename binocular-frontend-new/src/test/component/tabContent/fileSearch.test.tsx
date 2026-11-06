import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import FileSearch from '../../../components/tabs/fileTree/fileSearch/fileSearch.tsx';

describe('FileSearch', () => {
  it('C9.1 renders a text input', () => {
    render(<FileSearch setFileSearch={vi.fn()} />);
    expect(screen.getByRole('textbox')).toBeInTheDocument();
  });

  it('C9.2 input is empty by default', () => {
    render(<FileSearch setFileSearch={vi.fn()} />);
    const input = screen.getByRole('textbox') as HTMLInputElement;
    expect(input.value).toBe('');
  });

  it('C9.3 typing updates the input value', () => {
    render(<FileSearch setFileSearch={vi.fn()} />);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'src' } });
    expect((input as HTMLInputElement).value).toBe('src');
  });

  it('C9.4 onChange callback is called after typing', () => {
    const setFileSearch = vi.fn();
    render(<FileSearch setFileSearch={setFileSearch} />);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'src' } });
    expect(setFileSearch).toHaveBeenCalledWith('src');
  });

  it('C9.5 clearing the input calls onChange with empty string', () => {
    const setFileSearch = vi.fn();
    render(<FileSearch setFileSearch={setFileSearch} />);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'something' } });
    fireEvent.change(input, { target: { value: '' } });
    expect(setFileSearch).toHaveBeenLastCalledWith('');
  });
});
