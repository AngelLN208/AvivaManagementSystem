import apiClient from './client.js';

export function loginPatient(username, password) {
  return apiClient.post('/auth/login', { username, password });
}

export function registerPatient(patientData) {
  return apiClient.post('/auth/register-patient', patientData);
}
