import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, act } from '@testing-library/react';

// ── Actual imports ────────────────────────────────────────────────────────────

import PopoutController from '../../../components/dashboard/dashboardItemPopout/popoutController/popoutController.tsx';

// ── Default props ─────────────────────────────────────────────────────────────

const defaultOptions = { width: 800, height: 600 };

function makeProps(overrides: Partial<React.ComponentProps<typeof PopoutController>> = {}) {
  return {
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

  it('C34.1: window.open is called on mount', () => {
    const props = makeProps();
    render(<PopoutController {...props} />);
    expect(window.open).toHaveBeenCalled();
    const firstArg = (window.open as ReturnType<typeof vi.spyOn>).mock.calls[0][0];
    expect(firstArg).toBe('');
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

  it('C34.5: when the beforeunload event fires on the popout window, onClosing is called', async () => {
    const onClosing = vi.fn();
    const props = makeProps({ onClosing });

    await act(async () => {
      render(<PopoutController {...props} />);
    });

    // Allow React state updates (container set) and the second useEffect to run
    await act(async () => {
      // Flush pending microtasks / state updates
    });

    // Retrieve the 'beforeunload' listener that was registered on the mock window
    const beforeUnloadCall = mockWindow.addEventListener.mock.calls.find((call) => call[0] === 'beforeunload');
    expect(beforeUnloadCall).toBeDefined();
    const beforeUnloadHandler = beforeUnloadCall![1] as () => void;

    // Simulate the popout window closing
    beforeUnloadHandler();

    expect(onClosing).toHaveBeenCalled();
  });
});
