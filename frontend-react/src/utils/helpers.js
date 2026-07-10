export function getIniciales(nombre) {
  return (nombre || '')
    .split(' ')
    .map(w => w[0] || '')
    .join('')
    .substring(0, 2)
    .toUpperCase();
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('username');
  window.location.href = '/login'; // ajustamos la ruta cuando migremos login
}