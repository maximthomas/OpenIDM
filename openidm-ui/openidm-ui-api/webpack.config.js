/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

"use strict";

/**
 * =============================================================================
 * GRUNT → WEBPACK MIGRATION MAP  (openidm-ui-api)
 * =============================================================================
 * | Grunt task            | Webpack equivalent            |
 * |-----------------------|-------------------------------|
 * | grunt-contrib-copy    | copy-webpack-plugin           |
 * |   copy:swagger        |   swagger-ui-dist → target/www|
 * |   copy:resources      |   src/main/resources → target/www |
 * | grunt build:dev       | npm run build:dev             |
 * | grunt build:prod      | npm run build:prod / build    |
 * =============================================================================
 *
 * This module has no JS bundling — it only copies swagger-ui-dist assets
 * and static resources into target/www.
 */

const path       = require("path");
const CopyPlugin = require("copy-webpack-plugin");

const TARGET_DIR = path.resolve(__dirname, "target/www");
const SWAGGER_UI_DIST_DIR = path.resolve(__dirname, "node_modules/swagger-ui-dist");
const STATIC_RESOURCES_DIR = path.resolve(__dirname, "src/main/resources");
const SWAGGER_ASSETS = [
    "swagger-ui-bundle.js",
    "swagger-ui-standalone-preset.js",
    "swagger-ui.css"
];

module.exports = {
    mode:  "none",

    // No real JS entry — this config exists only to run CopyPlugin.
    entry: {},

    output: {
        path:     TARGET_DIR,
        filename: "[name].js"
    },

    plugins: [
        new CopyPlugin({
            patterns: [
                // copy:swagger — only the three required swagger-ui-dist files
                ...SWAGGER_ASSETS.map((asset) => ({
                    from: path.join(SWAGGER_UI_DIST_DIR, asset),
                    to: TARGET_DIR,
                    noErrorOnMissing: false
                })),
                // copy:resources — all static resources
                {
                    from: STATIC_RESOURCES_DIR,
                    to: TARGET_DIR,
                    noErrorOnMissing: true,
                    globOptions: { dot: true }
                }
            ]
        })
    ],

    stats: { assets: true, modules: false, errors: true }
};
