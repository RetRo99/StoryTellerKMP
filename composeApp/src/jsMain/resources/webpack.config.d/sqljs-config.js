config.devServer.proxy = {
    '/proxy': {
        target: 'https://audiobooks.dev',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/proxy': '' },
    },
};

config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;
config.resolve.fallback.os = false;

if (config.devServer && config.devServer.client && config.devServer.client.overlay) {
    config.devServer.client.overlay.errors = false;
    config.devServer.client.overlay.warnings = false;
}

var CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: require('path').resolve(__dirname, '../../../node_modules/sql.js/dist/sql-wasm.wasm'),
                to: 'sql-wasm.wasm'
            }
        ]
    })
);
