import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { NodeGlobalsPolyfillPlugin } from '@esbuild-plugins/node-globals-polyfill';
import { NodeModulesPolyfillPlugin } from '@esbuild-plugins/node-modules-polyfill';
import ConditionalCompile from 'vite-plugin-conditional-compiler';
import { viteSingleFile } from 'vite-plugin-singlefile';
import { nodePolyfills } from 'vite-plugin-node-polyfills';
import { readFileSync, existsSync } from 'node:fs';
import { resolve as pathResolve, dirname } from 'node:path';
import JSZip from 'jszip';

function jsonZipPlugin() {
  return {
    name: 'vite-plugin-json-zip',
    enforce: 'pre' as const,
    resolveId(id: string, importer: string | undefined) {
      if (!id.includes('.json.zip') || !importer) return null;
      const bare = id.split('?')[0];
      const query = id.includes('?') ? '?' + id.split('?')[1] : '';
      if (pathResolve(bare) === bare) return null; // already absolute
      const resolved = pathResolve(dirname(importer.split('?')[0]), bare);
      if (existsSync(resolved)) return resolved + query;
      return null;
    },
    async load(id: string) {
      if (!id.endsWith('.json.zip')) return null;
      const zip = await JSZip.loadAsync(readFileSync(id));
      const name = Object.keys(zip.files).find((n) => n.endsWith('.json'))!;
      const json = await zip.file(name)!.async('string');
      return `export default ${JSON.stringify(JSON.parse(json))}`;
    },
  };
}

// https://vitejs.dev/config/
const backendHost = process.env.BACKEND_URL ?? 'localhost';
export default defineConfig({
  assetsInclude: ['**/*.json.zip'],
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: `http://${backendHost}:48763/`,
        secure: false,
      },
      '/graphQl': {
        target: `http://${backendHost}:48763/`,
        secure: false,
        changeOrigin: true,
      },
      '/wsapi': {
        target: `ws://${backendHost}:48763`,
        ws: true,
      },
    },
  },
  plugins: [jsonZipPlugin(), nodePolyfills(), react(), ConditionalCompile(), viteSingleFile()],
  build: {
    emptyOutDir: true,
    outDir: '../dist',
  },
  optimizeDeps: {
    exclude: [],
    esbuildOptions: {
      loader: {
        '.js': 'jsx',
      },
      // Enable esbuild polyfill plugins
      plugins: [
        NodeGlobalsPolyfillPlugin({
          process: true,
          buffer: true,
        }),
        NodeModulesPolyfillPlugin(),
      ],
      // Node.js global to browser globalThis
      define: {
        global: 'globalThis',
      },
      // resolveExtensions: ['.tsx', '.ts', '.js']
    },
  },
  define: {
    global: 'globalThis',
  },
  worker: {
    format: 'es',
  },
});
