import js from '@eslint/js';
import globals from 'globals';

export default [
  { ignores: ['dist', 'coverage', 'src', 'vite.config.ts'] },
  {
    ...js.configs.recommended,
    files: ['*.js'],
    languageOptions: { ecmaVersion: 2022, sourceType: 'module', globals: globals.node },
  },
];

