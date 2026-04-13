import { defineConfig } from "eslint/config";
import globals from "globals";
import babelParser from "@babel/eslint-parser";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});

export default defineConfig([{
    extends: compat.extends(),

    languageOptions: {
        globals: {
            ...globals.amd,
            ...globals.browser,
            ...globals.qunit,
        },

        parser: babelParser,
    },

    rules: {
        indent: [1, 4, {
            SwitchCase: 1,
            VariableDeclarator: 1,
        }],

        "no-alert": 2,
        "no-catch-shadow": 2,
        "no-duplicate-case": 2,
        "no-empty-character-class": 2,
        "no-extend-native": 2,
        "no-invalid-regexp": 2,
        "no-irregular-whitespace": 2,
        "no-labels": 2,
        "no-mixed-spaces-and-tabs": 2,
        "no-multiple-empty-lines": 2,
        "no-multi-str": 2,
        "no-native-reassign": 2,
        "no-void": 2,
        "arrow-parens": [2, "always"],
        "arrow-spacing": 2,
    },
}]);