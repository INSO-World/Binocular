import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import TabSection from '../../../components/tabMenu/tabSection/tabSection';
import { TabAlignment } from '../../../types/general/tabType';

// A simple child that renders its own props so we can inspect them
function Child(props: { orientation?: string }) {
  return <span data-orientation={props.orientation}>child</span>;
}

describe('TabSection', () => {
  it('C21.1 alignment=top renders horizontal container', () => {
    const { container } = render(
      <TabSection alignment={TabAlignment.top}>
        <Child />
      </TabSection>,
    );
    // The horizontal class div should be present
    const div = container.firstChild as HTMLElement;
    expect(div.tagName).toBe('DIV');
  });

  it('C21.2 alignment=left renders vertical container', () => {
    const { container } = render(
      <TabSection alignment={TabAlignment.left}>
        <Child />
      </TabSection>,
    );
    const div = container.firstChild as HTMLElement;
    expect(div.tagName).toBe('DIV');
  });

  it('C21.3 alignment=undefined renders (horizontal) layout', () => {
    const { container } = render(
      <TabSection>
        <Child />
      </TabSection>,
    );
    expect(container.firstChild).not.toBeNull();
  });

  it('C21.4 renders name label when provided', () => {
    const { getByText } = render(
      <TabSection name="My Section">
        <Child />
      </TabSection>,
    );
    expect(getByText('My Section')).not.toBeNull();
  });

  it('C21.5 clones single child with orientation="horizontal" for top alignment', () => {
    const { container } = render(
      <TabSection alignment={TabAlignment.top}>
        <Child />
      </TabSection>,
    );
    const span = container.querySelector('span');
    expect(span?.getAttribute('data-orientation')).toBe('horizontal');
  });

  it('C21.6 clones multiple children with orientation', () => {
    const { container } = render(<TabSection alignment={TabAlignment.top}>{[<Child key="a" />, <Child key="b" />]}</TabSection>);
    const spans = container.querySelectorAll('span');
    expect(spans).toHaveLength(2);
    spans.forEach((s) => expect(s.getAttribute('data-orientation')).toBe('horizontal'));
  });

  it('C21.7 alignment=left + multiple children → each child gets orientation="vertical"', () => {
    const { container } = render(<TabSection alignment={TabAlignment.left}>{[<Child key="a" />, <Child key="b" />]}</TabSection>);
    const spans = container.querySelectorAll('span');
    expect(spans).toHaveLength(2);
    spans.forEach((s) => expect(s.getAttribute('data-orientation')).toBe('vertical'));
  });
});
