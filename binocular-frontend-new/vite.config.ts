import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { NodeGlobalsPolyfillPlugin } from '@esbuild-plugins/node-globals-polyfill';
import { NodeModulesPolyfillPlugin } from '@esbuild-plugins/node-modules-polyfill';
import ConditionalCompile from 'vite-plugin-conditional-compiler';
import { viteSingleFile } from 'vite-plugin-singlefile';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

function get_env_var(env: Record<any, any>, name: string, default_val: any) {
  if (env.BACKEND_URL) {
    return env.BACKEND_URL;
  } else {
    return default_val;
  }
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  const BACKEND_URL = get_env_var(env, 'BACKEND_URL', 'localhost');
  console.info(`BACKEND_URL: ${BACKEND_URL}`);

  return {
    server: {
      port: 8080,
      proxy: {
        '/api': {
          target: `http://${BACKEND_URL}:48763/`,
          secure: false,
        },
        '/graphQl': {
          target: `http://${BACKEND_URL}:48763/`,
          secure: false,
          changeOrigin: true,
        },
        '/wsapi': {
          target: `ws://${BACKEND_URL}:48763`,
          ws: true,
        },
      },
    },
    plugins: [nodePolyfills(), react(), ConditionalCompile(), viteSingleFile()],
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
  }
});
