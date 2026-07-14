import apiClient from './client.js';

export function loginPatient(username, password) {
  return apiClient.post('/auth/login', { username, password });
}

export function registerPatient(patientData) {
  return apiClient.post('/auth/register-patient', patientData);
}

export function requestPatientActivation(dni) {
  return apiClient.post('/auth/patient-activation/request', { dni });
}

export function verifyPatientActivationCode(verificationData) {
  return apiClient.post('/auth/patient-activation/verify-code', verificationData);
}

export function completePatientActivation(accessData) {
  return apiClient.post('/auth/patient-activation/complete', accessData);
}
