import client from './client';

export const getInsurances = () => client.get('/insurances');
export const linkInsuranceToPatient = (patientId, payload) =>
  client.post(`/patient-insurances/patient/${patientId}`, payload);