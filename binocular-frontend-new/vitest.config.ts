import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: [
        'src/plugins/visualizationPlugins/**/*.{ts,tsx}',
        'src/components/**/*.{ts,tsx}',
        'src/redux/reducer/**/*.ts',
        'src/redux/middleware/**/*.ts',
        'src/utils/*.{ts,tsx}',
      ],
      exclude: [
        'src/test/**',
        'src/plugins/visualizationPlugins/example',
        'src/plugins/visualizationPlugins/**/index.{ts,tsx}',
        'src/plugins/visualizationPlugins/**/help.tsx',
        'src/plugins/visualizationPlugins/**/src/chart/*.tsx', // could be changed to only Chart.tsx
        'src/plugins/visualizationPlugins/**/src/settings/**',
        'src/plugins/visualizationPlugins/**/src/saga/**',
        'src/components/tabMenu/tabMenuContent/**',
        'src/components/dashboard/reduxSubAppStoreWrapper/**',
      ],
    },
  },
});
