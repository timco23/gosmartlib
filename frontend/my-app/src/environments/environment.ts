export const environment = {
  production: false,
  get apiUrl() {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname.startsWith('192.168')) {
      return '/api';
    }
    if (hostname.startsWith('app.')) {
      return `https://${hostname.replace('app.', 'api.')}`;
    }
    return `https://api.${hostname}`;
  }
};