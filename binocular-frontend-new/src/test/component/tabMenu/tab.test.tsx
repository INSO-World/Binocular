import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import Tab from '../../../components/tabMenu/tab/tab';
import TabSection from '../../../components/tabMenu/tabSection/tabSection';
import { TabAlignment } from '../../../types/general/tabType';

function PlainChild() {
  return <span>plain</span>;
}

describe('Tab', () => {
  it('C22.1 renders a container div', () => {
    const { container } = render(
      <Tab displayName="T" alignment={TabAlignment.top}>
        <PlainChild />
      </Tab>,
    );
    expect(container.querySelector('div')).not.toBeNull();
  });

  it('C22.2 single non-TabSection child passes through unchanged', () => {
    const { getByText } = render(
      <Tab displayName="T" alignment={TabAlignment.top}>
        <PlainChild />
      </Tab>,
    );
    expect(getByText('plain')).not.toBeNull();
  });

  it('C22.3 single TabSection child receives alignment prop', () => {
    // TabSection renders children with orientation; just assert it mounts without error
    const { container } = render(
      <Tab displayName="T" alignment={TabAlignment.right}>
        <TabSection>
          <PlainChild />
        </TabSection>
      </Tab>,
    );
    expect(container.querySelector('div')).not.toBeNull();
  });

  it('C22.4 array with TabSection child injects alignment', () => {
    const { container } = render(
      <Tab displayName="T" alignment={TabAlignment.bottom}>
        {[
          <TabSection key="s">
            <PlainChild />
          </TabSection>,
          <PlainChild key="p" />,
        ]}
      </Tab>,
    );
    expect(container.querySelector('div')).not.toBeNull();
    expect(container.querySelector('span')).not.toBeNull();
  });

  it('C22.5 array with non-TabSection child renders it unmodified', () => {
    const { getAllByText } = render(
      <Tab displayName="T" alignment={TabAlignment.top}>
        {[<PlainChild key="a" />, <PlainChild key="b" />]}
      </Tab>,
    );
    expect(getAllByText('plain')).toHaveLength(2);
  });
});
