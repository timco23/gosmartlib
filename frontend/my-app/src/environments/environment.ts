export const environment = {
  production: false,
  get apiUrl() {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname.startsWith('192.168')) {
      return '/api';
    }
    if (hostname.startsWith('app.')) {
      return `http://${hostname.replace('app.', 'api.')}`;
    }
    // Voor gsmartlib.tech → api.gsmartlib.tech
    return `http://api.${hostname}`;
  }
};