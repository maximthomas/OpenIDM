// babel.config.js
module.exports = {
  presets: [
    [
      "@babel/preset-env",
      {
        // Targets specific environments (e.g., last 2 versions of browsers)
        targets: "> 0.25%, not dead",
        // Automatically imports only necessary polyfills
        useBuiltIns: "usage",
        corejs: 3,
      },
    ],
  ],
};