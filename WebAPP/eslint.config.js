import js from '@eslint/js'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'

export default [
  { ignores: ['dist'] },
  {
    files: ['**/*.{js,jsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: { window: true, document: true, localStorage: true, console: true, setTimeout: true, clearInterval: true, setInterval: true, URL: true, URLSearchParams: true, FormData: true, FileReader: true, Promise: true, parseInt: true, isNaN: true, Math: true, Date: true, JSON: true, String: true, Number: true, Array: true, Object: true, Error: true },
      parserOptions: { ecmaVersion: 'latest', ecmaFeatures: { jsx: true }, sourceType: 'module' },
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      'no-unused-vars': 'warn',
    },
  },
]
