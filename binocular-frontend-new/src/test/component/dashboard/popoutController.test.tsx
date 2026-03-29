import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, act } from '@testing-library/react';

// ── Actual imports ────────────────────────────────────────────────────────────

import PopoutController from '../../../components/dashboard/dashboardItemPopout/popoutController/popoutController.tsx';

// ── Default props ─────────────────────────────────────────────────────────────

const defaultOptions = { width: 800, height: 600 };

function makeProps(overrides: Partial<React.ComponentProps<typeof PopoutController>> = {}) {
  return {
    url: 'about:blank',
    title: 'Test Popout',
    options: defaultOptions,
    onClosing: vi.fn(),
    onError: vi.fn(),
    onResize: vi.fn(),
    children: <div data-testid="child-content">Child</div>,
    ...overrides,
  };
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('PopoutController', () => {
  let mockPopoutDoc: Document;
  let mockWindow: {
    document: Document;
    closed: boolean;
    close: ReturnType<typeof vi.fn>;
    addEventListener: ReturnType<typeof vi.fn>;
    removeEventListener: ReturnType<typeof vi.fn>;
    focus: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockPopoutDoc = document.implementation.createHTMLDocument('popout');
    mockWindow = {
      document: mockPopoutDoc,
      closed: false,
      close: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      focus: vi.fn(),
    };
    vi.spyOn(window, 'open').mockReturnValue(mockWindow as unknown as Window);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('C34.1: window.open is called on mount with props.url', () => {
    const props = makeProps({ url: 'about:blank' });
    render(<PopoutController {...props} />);
    expect(window.open).toHaveBeenCalled();
    const firstArg = (window.open as ReturnType<typeof vi.spyOn>).mock.calls[0][0];
    expect(firstArg).toBe('about:blank');
  });

  it('C34.2: window.open is called with props.title as the second argument', () => {
    const props = makeProps({ title: 'My Popout Title' });
    render(<PopoutController {...props} />);
    expect(window.open).toHaveBeenCalled();
    const secondArg = (window.open as ReturnType<typeof vi.spyOn>).mock.calls[0][1];
    expect(secondArg).toBe('My Popout Title');
  });

  it('C34.3: when window.open returns null, onError callback is called', () => {
    vi.spyOn(window, 'open').mockReturnValue(null);
    const onError = vi.fn();
    const props = makeProps({ onError });
    render(<PopoutController {...props} />);
    expect(onError).toHaveBeenCalledOnce();
  });

  it('C34.4: component renders nothing to the main DOM (container is empty)', () => {
    const props = makeProps();
    const { container } = render(<PopoutController {...props} />);
    // PopoutController returns null — only the wrapping div from render() should exist, and it should be empty
    expect(container.firstChild).toBeNull();
  });

  it('C34.5: when mockWindow.closed becomes true and poll interval fires, onClosing is called', async () => {
    vi.useFakeTimers();
    const onClosing = vi.fn();
    const props = makeProps({ onClosing });

    await act(async () => {
      render(<PopoutController {...props} />);
    });

    // Allow React state updates (setPopoutWindow) and the second useEffect to run
    await act(async () => {
      // Flush pending microtasks / state updates
    });

    // Now make the mock window appear closed
    mockWindow.closed = true;

    // Advance past the 500ms polling interval
    await act(async () => {
      vi.advanceTimersByTime(600);
    });

    expect(onClosing).toHaveBeenCalled();
  });
});
