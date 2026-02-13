export const environment = {
  production: false,
  get apiUrl() {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname.startsWith('192.168')) {
      return '/api';
    }
    return `http://${hostname.replace('app.', 'api.')}`; 
  }
};