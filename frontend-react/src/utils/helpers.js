export function getIniciales(nombre) {
  return (nombre || '')
    .split(' ')
    .map(w => w[0] || '')
    .join('')
    .substring(0, 2)
    .toUpperCase();
}

export function parseJwt(token) {
  return JSON.parse(atob(token.split('.')[1]));
}