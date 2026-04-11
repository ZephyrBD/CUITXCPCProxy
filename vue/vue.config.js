const path = require('path');

module.exports = {
  outputDir: path.resolve(__dirname, '../src/main/resources/static'),
  publicPath: '/cxtool/',
  devServer: {
    port: 8081,
    proxy: {
      '/cxtool': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
}