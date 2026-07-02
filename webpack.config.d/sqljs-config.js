config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;

if (config.devServer && config.devServer.client && config.devServer.client.overlay) {
    config.devServer.client.overlay.errors = false;
    config.devServer.client.overlay.warnings = false;
}
