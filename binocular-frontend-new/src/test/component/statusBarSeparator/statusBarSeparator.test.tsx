import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import StatusBarSeparator from '../../../components/statusBar/statusBarSeparator/statusBarSeparator';

describe('StatusBarSeparator', () => {
  it('C14.1 direction="horizontal" renders a <span>', () => {
    const { container } = render(<StatusBarSeparator direction="horizontal" />);
    expect(container.querySelector('span')).not.toBeNull();
  });

  it('C14.2 direction="vertical" renders a <span>', () => {
    const { container } = render(<StatusBarSeparator direction="vertical" />);
    expect(container.querySelector('span')).not.toBeNull();
  });

  it('C14.3 direction="diagonal" renders a <span>', () => {
    const { container } = render(<StatusBarSeparator direction="diagonal" />);
    expect(container.querySelector('span')).not.toBeNull();
  });

  it('C14.4 unknown direction renders nothing (no <span>)', () => {
    const { container } = render(<StatusBarSeparator direction="unknown" />);
    expect(container.querySelector('span')).toBeNull();
  });
});
