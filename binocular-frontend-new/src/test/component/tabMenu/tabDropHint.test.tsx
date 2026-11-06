import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import TabDropHint from '../../../components/tabMenu/tabController/tabDropHint/tabDropHint';

describe('TabDropHint', () => {
  it('C25.1 dragState=false renders no drop zones', () => {
    const { queryAllByText } = render(<TabDropHint dragState={false} />);
    expect(queryAllByText('Drop Here')).toHaveLength(0);
  });

  it('C25.2 dragState=true renders 4 "Drop Here" zones', () => {
    const { getAllByText } = render(<TabDropHint dragState={true} />);
    expect(getAllByText('Drop Here')).toHaveLength(4);
  });

  it('C25.3 switching from false to true shows drop zones', () => {
    const { rerender, getAllByText, queryAllByText } = render(<TabDropHint dragState={false} />);
    expect(queryAllByText('Drop Here')).toHaveLength(0);
    rerender(<TabDropHint dragState={true} />);
    expect(getAllByText('Drop Here')).toHaveLength(4);
  });
});
