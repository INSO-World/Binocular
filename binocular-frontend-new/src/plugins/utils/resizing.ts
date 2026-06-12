import type { Store } from '@reduxjs/toolkit';
import { throttle } from 'throttle-debounce';

export function handlePopoutResizing(store: Store, fn: () => void): () => void {
  /**
   * Throttle the resize of the svg (refresh rate) to every 100ms to not overwhelm the renderer,
   * This isn't really necessary for this visualization, but for bigger visualization this can be quite essential
   */
  const throttledResize = throttle(100, fn, { noLeading: false, noTrailing: false });

  return store.subscribe(() => {
    if (store !== undefined) {
      if (store.getState().actions.lastAction === 'RESIZE') {
        throttledResize();
      }
    }
  });
}
