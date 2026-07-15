import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { getTriajePorCita } from '../../api/triajesApi';
import { registrarConsulta } from '../../api/consultasApi';

export default function AtencionClinica() {
    const { appointmentId } = useParams();
    const navigate = useNavigate();
    const primaryColor = "#00a4e4";

    const [diagnostico, setDiagnostico] = useState('');
    const [tratamiento, setTratamiento] = useState('');
    const [notasAdicionales, setNotasAdicionales] = useState('');

    const { data: triajeData, isLoading: isLoadingTriaje } = useQuery({
        queryKey: ['triaje', appointmentId],
        queryFn: () => getTriajePorCita(appointmentId),
        retry: false,
    });

    const mutationConsulta = useMutation({
        mutationFn: (payload) => registrarConsulta(appointmentId, payload),
        onSuccess: () => navigate('/doctor/citas'),
        onError: () => alert('No se pudo registrar la consulta. Verifique estado de cita.')
    });

    const triaje = triajeData?.data?.data;

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!diagnostico.trim() || !tratamiento.trim()) {
            alert('El diagnóstico y el tratamiento son obligatorios.');
            return;
        }
        mutationConsulta.mutate({ diagnosis: diagnostico, treatment: tratamiento, notes: notasAdicionales });
    };

    return (
        <div className="container-fluid p-0">
            {/* Header */}
            <div className="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
                <div>
                    <h2 className="h4 fw-bold mb-1" style={{ color: '#004b87' }}>Sala de Atención Clínica</h2>
                    <span className="badge bg-light text-dark border">CITA #{appointmentId}</span>
                </div>
                <button onClick={() => navigate('/doctor/citas')} className="btn btn-outline-secondary">
                    <i className="bi bi-arrow-left me-2"></i>Volver a la Agenda
                </button>
            </div>

            <div className="row g-4">
                {/* Panel Triaje */}
                <div className="col-lg-4">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-header bg-white border-bottom p-3">
                            <h6 className="fw-bold mb-0" style={{ color: primaryColor }}>
                                <i className="bi bi-heart-pulse-fill me-2"></i>Signos Vitales
                            </h6>
                        </div>
                        <div className="card-body">
                            {isLoadingTriaje ? (
                                <div className="text-center text-muted my-4"><div className="spinner-border spinner-border-sm me-2"></div>Cargando...</div>
                            ) : !triaje ? (
                                <div className="alert alert-warning text-center small">No hay triaje registrado por enfermería.</div>
                            ) : (
                                <ul className="list-group list-group-flush">
                                    <li className="list-group-item d-flex justify-content-between px-0">
                                        <span className="text-muted">Presión Arterial</span>
                                        <strong>{triaje.systolicPressure}/{triaje.diastolicPressure} <small className="fw-normal">mmHg</small></strong>
                                    </li>
                                    <li className="list-group-item d-flex justify-content-between px-0">
                                        <span className="text-muted">Temperatura</span>
                                        <strong>{triaje.temperature} <small className="fw-normal">°C</small></strong>
                                    </li>
                                    <li className="list-group-item d-flex justify-content-between px-0">
                                        <span className="text-muted">Frec. Cardíaca</span>
                                        <strong>{triaje.heartRate} <small className="fw-normal">lpm</small></strong>
                                    </li>
                                    <li className="list-group-item d-flex justify-content-between px-0">
                                        <span className="text-muted">Peso</span>
                                        <strong>{triaje.weight} <small className="fw-normal">kg</small></strong>
                                    </li>
                                </ul>
                            )}
                        </div>
                    </div>
                </div>

                {/* Panel Formulario */}
                <div className="col-lg-8">
                    <div className="card border-0 shadow-sm">
                        <div className="card-header bg-white border-bottom p-3">
                            <h6 className="fw-bold mb-0 text-dark">
                                <i className="bi bi-file-earmark-medical-fill me-2" style={{ color: primaryColor }}></i>Registro Médico
                            </h6>
                        </div>
                        <div className="card-body p-4">
                            <form onSubmit={handleSubmit}>
                                <div className="mb-4">
                                    <label className="form-label fw-bold">Diagnóstico Clínico <span className="text-danger">*</span></label>
                                    <textarea 
                                        className="form-control bg-light" 
                                        rows="3" 
                                        required 
                                        value={diagnostico} 
                                        onChange={(e) => setDiagnostico(e.target.value)}
                                        placeholder="Ingrese el diagnóstico..."
                                    ></textarea>
                                </div>
                                <div className="mb-4">
                                    <label className="form-label fw-bold">Tratamiento / Receta <span className="text-danger">*</span></label>
                                    <textarea 
                                        className="form-control bg-light" 
                                        rows="4" 
                                        required 
                                        value={tratamiento} 
                                        onChange={(e) => setTratamiento(e.target.value)}
                                        placeholder="Medicamentos, dosis, indicaciones..."
                                    ></textarea>
                                </div>
                                <div className="mb-4">
                                    <label className="form-label fw-bold text-muted">Notas Adicionales (Opcional)</label>
                                    <textarea 
                                        className="form-control bg-light" 
                                        rows="2" 
                                        value={notasAdicionales} 
                                        onChange={(e) => setNotasAdicionales(e.target.value)}
                                    ></textarea>
                                </div>
                                <div className="text-end border-top pt-3 mt-4">
                                    <button 
                                        type="submit" 
                                        disabled={mutationConsulta.isPending} 
                                        className="btn text-white px-4 py-2"
                                        style={{ backgroundColor: primaryColor }}
                                    >
                                        {mutationConsulta.isPending ? 'Guardando...' : <><i className="bi bi-check-circle me-2"></i>Finalizar Consulta</>}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}