// ESLint Flat Config (ESLint v9+)
// Documentação: https://eslint.org/docs/latest/use/configure/

const js = require('@eslint/js');
const tsPlugin = require('@typescript-eslint/eslint-plugin');
const tsParser = require('@typescript-eslint/parser');
const prettierConfig = require('eslint-config-prettier');
const importPlugin = require('eslint-plugin-import');

module.exports = [
  // Arquivos/diretórios ignorados
  {
    ignores: [
      'node_modules',
      'target',
      'dist',
      '*.log',
      '**/*.java',
      // Build de front caso adicionado futuramente
      'build',
    ],
  },
  // Regras base JavaScript
  js.configs.recommended,
  // Regras TypeScript
  {
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      parser: tsParser,
      ecmaVersion: 2022,
      sourceType: 'module',
    },
    plugins: {
      '@typescript-eslint': tsPlugin,
    },
    rules: {
      ...tsPlugin.configs.recommended.rules,
      '@typescript-eslint/no-unused-vars': [
        'warn',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
    },
  },
  // Regras para import/order e validações de import
  {
    files: ['**/*.{js,cjs,mjs,ts,tsx}'],
    plugins: { import: importPlugin },
    rules: {
      ...importPlugin.configs.recommended.rules,
      'import/order': [
        'warn',
        {
          groups: ['builtin', 'external', 'internal', 'parent', 'sibling', 'index'],
          'newlines-between': 'always',
          alphabetize: { order: 'asc', caseInsensitive: true },
        },
      ],
    },
  },
  // Desliga conflitos com Prettier
  prettierConfig,
];
