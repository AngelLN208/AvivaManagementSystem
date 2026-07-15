import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { getTriajePorCita } from '../../api/triajesApi';
import { registrarConsulta } from '../../api/consultasApi';

export default function AtencionClinica() {
    const { appointmentId } = useParams();
    const navigate = useNavigate();

    // Estado del formulario clínico
    const [diagnostico, setDiagnostico] = useState('');
    const [tratamiento, setTratamiento] = useState('');
    const [notasAdicionales, setNotasAdicionales] = useState('');

    // 1. Obtener los signos vitales (Triaje)
    const { data: triajeData, isLoading: isLoadingTriaje } = useQuery({
        queryKey: ['triaje', appointmentId],
        queryFn: () => getTriajePorCita(appointmentId),
        retry: false,
    });

    // 2. Mutación para guardar la consulta
    const mutationConsulta = useMutation({
        mutationFn: (payload) => registrarConsulta(appointmentId, payload),
        onSuccess: () => {
            navigate('/doctor/citas');
        },
        onError: (error) => {
            console.error('Error al registrar consulta:', error);
            alert('No se pudo registrar la consulta. Verifique que la cita esté CONFIRMED y no tenga una consulta previa.');
        }
    });

    const triaje = triajeData?.data?.data;

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!diagnostico.trim() || !tratamiento.trim()) {
            alert('El diagnóstico y el tratamiento son obligatorios.');
            return;
        }

        mutationConsulta.mutate({
            diagnosis: diagnostico,
            treatment: tratamiento,
            notes: notasAdicionales
        });
    };

    return (
        <div className="max-w-7xl mx-auto h-full flex flex-col animation-fade-in font-sans">
            {/* Cabecera */}
            <header className="mb-6 flex justify-between items-end border-b pb-4 border-gray-200">
                <div>
                    <h2 className="text-2xl font-bold text-[#004b87]"> {/* Azul Marino Aviva */}
                        Sala de Atención Clínica
                    </h2>
                    <p className="text-gray-500 font-medium mt-1 text-sm">
                        GESTIÓN DE CITA <span className="text-[#ff6a13]">#{appointmentId}</span> {/* Naranja Aviva */}
                    </p>
                </div>
                <button 
                    onClick={() => navigate('/doctor/citas')}
                    className="px-4 py-2 border border-[#004b87] text-[#004b87] hover:bg-[#004b87]/5 transition-colors rounded text-sm font-medium flex items-center gap-2"
                >
                    <span>←</span> Volver a la Agenda
                </button>
            </header>

            <div className="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-6">
                
                {/* PANEL IZQUIERDO: Triaje e Información */}
                <div className="lg:col-span-1 space-y-6">
                    <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
                        <div className="flex items-center gap-2 mb-4 border-b border-gray-100 pb-3">
                            <span className="text-[#00a4e4] text-xl">♥</span> {/* Celeste Aviva */}
                            <h3 className="text-slate-800 font-bold text-lg">
                                Signos Vitales
                            </h3>
                        </div>
                        
                        {isLoadingTriaje ? (
                            <p className="text-[#004b87]/50 font-medium text-sm animate-pulse">Recopilando datos...</p>
                        ) : !triaje ? (
                            <div className="bg-gray-50 text-gray-500 p-4 rounded text-sm border border-gray-100 text-center">
                                No se registró triaje para esta cita.
                            </div>
                        ) : (
                            <div className="space-y-3">
                                <div className="flex justify-between items-center border-b border-gray-50 pb-2">
                                    <span className="text-gray-500 font-medium text-sm">Presión Arterial</span>
                                    <span className="text-slate-800 font-bold">{triaje.systolicPressure}/{triaje.diastolicPressure} <span className="text-xs font-normal text-gray-400">mmHg</span></span>
                                </div>
                                <div className="flex justify-between items-center border-b border-gray-50 pb-2">
                                    <span className="text-gray-500 font-medium text-sm">Temperatura</span>
                                    <span className="text-slate-800 font-bold">{triaje.temperature} <span className="text-xs font-normal text-gray-400">°C</span></span>
                                </div>
                                <div className="flex justify-between items-center border-b border-gray-50 pb-2">
                                    <span className="text-gray-500 font-medium text-sm">Frec. Cardíaca</span>
                                    <span className="text-slate-800 font-bold">{triaje.heartRate} <span className="text-xs font-normal text-gray-400">lpm</span></span>
                                </div>
                                <div className="flex justify-between items-center border-b border-gray-50 pb-2">
                                    <span className="text-gray-500 font-medium text-sm">Frec. Respiratoria</span>
                                    <span className="text-slate-800 font-bold">{triaje.respiratoryRate} <span className="text-xs font-normal text-gray-400">rpm</span></span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-gray-500 font-medium text-sm">Peso / Altura</span>
                                    <span className="text-slate-800 font-bold">{triaje.weight} <span className="text-xs font-normal text-gray-400">kg</span> / {triaje.height} <span className="text-xs font-normal text-gray-400">cm</span></span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* PANEL DERECHO: Formulario de Consulta */}
                <div className="lg:col-span-2">
                    <form 
                        onSubmit={handleSubmit}
                        className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm h-full flex flex-col"
                    >
                        <div className="flex items-center gap-2 mb-6 border-b border-gray-100 pb-3">
                            <span className="text-[#004b87] text-xl">✎</span>
                            <h3 className="text-slate-800 font-bold text-lg">
                                Registro Médico
                            </h3>
                        </div>

                        <div className="space-y-5 flex-1">
                            {/* Diagnóstico */}
                            <div>
                                <label className="block text-slate-700 font-bold text-sm mb-2">
                                    Diagnóstico (CIE-10 o Descripción) <span className="text-[#ff6a13]">*</span>
                                </label>
                                <textarea
                                    required
                                    rows="3"
                                    value={diagnostico}
                                    onChange={(e) => setDiagnostico(e.target.value)}
                                    className="w-full bg-white border border-gray-300 text-slate-800 focus:border-[#00a4e4] focus:ring-2 focus:ring-[#00a4e4]/20 rounded p-3 outline-none transition-all resize-none text-sm"
                                    placeholder="Ingrese el diagnóstico clínico detallado..."
                                />
                            </div>

                            {/* Tratamiento / Receta */}
                            <div>
                                <label className="block text-slate-700 font-bold text-sm mb-2">
                                    Tratamiento y Receta Médica <span className="text-[#ff6a13]">*</span>
                                </label>
                                <textarea
                                    required
                                    rows="4"
                                    value={tratamiento}
                                    onChange={(e) => setTratamiento(e.target.value)}
                                    className="w-full bg-white border border-gray-300 text-slate-800 focus:border-[#00a4e4] focus:ring-2 focus:ring-[#00a4e4]/20 rounded p-3 outline-none transition-all resize-none text-sm"
                                    placeholder="Describa los medicamentos, dosis y duración del tratamiento..."
                                />
                            </div>

                            {/* Notas Adicionales */}
                            <div>
                                <label className="block text-slate-700 font-bold text-sm mb-2">
                                    Notas Adicionales <span className="text-gray-400 font-normal">(Opcional)</span>
                                </label>
                                <textarea
                                    rows="2"
                                    value={notasAdicionales}
                                    onChange={(e) => setNotasAdicionales(e.target.value)}
                                    className="w-full bg-white border border-gray-300 text-slate-800 focus:border-[#00a4e4] focus:ring-2 focus:ring-[#00a4e4]/20 rounded p-3 outline-none transition-all resize-none text-sm"
                                    placeholder="Observaciones internas, recomendaciones de descanso, etc."
                                />
                            </div>
                        </div>

                        {/* Botón de Envío */}
                        <div className="mt-8 pt-5 border-t border-gray-100 flex justify-end">
                            <button
                                type="submit"
                                disabled={mutationConsulta.isPending}
                                className="px-8 py-2.5 bg-[#ff6a13] hover:bg-[#e55e0f] text-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-bold text-sm rounded shadow-sm"
                            >
                                {mutationConsulta.isPending ? 'Procesando...' : 'Finalizar Consulta'}
                            </button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    );
}