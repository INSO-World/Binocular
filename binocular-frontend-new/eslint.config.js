import { defineConfig } from "eslint/config";
import typescriptParser from "@typescript-eslint/parser";
import reactRefresh from "eslint-plugin-react-refresh";
import globals from "globals";
import js from "@eslint/js";
import TSESLint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import react from 'eslint-plugin-react'
import eslintPluginPrettierRecommended from "eslint-plugin-prettier/recommended";
export default defineConfig([
  {
    files: ["**/*.{js,mjs,cjs,ts,mts,jsx,tsx}"],
    rules: {
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],
      '@typescript-eslint/no-unsafe-return':0,
      '@typescript-eslint/no-unsafe-assignment':0,
      '@typescript-eslint/no-unsafe-call':0,
      '@typescript-eslint/no-unsafe-member-access':0,
      '@typescript-eslint/no-unsafe-argument':0,
      'prettier/prettier':[
        'error',
        {
          singleQuote: true,
          printWidth: 140,
          jsxBracketSameLine: true
        },
      ],
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          args: 'all',
          argsIgnorePattern: '^_',
        }
      ],
    },
    extends: [
      js.configs.recommended,
      TSESLint.configs.recommended,
      reactHooks.configs['recommended-latest'],
      react.configs.flat.recommended,
      react.configs.flat["jsx-runtime"],
      eslintPluginPrettierRecommended,
      reactRefresh.configs.recommended
    ],
    ignores: ['dist', 'eslint.config.js'],
    languageOptions: {
      parser: typescriptParser,
      ecmaVersion: 2023,
      sourceType: "module",
      globals: {
        ...globals.browser,
        ...globals.es2020
      }
    }
  }
]);
