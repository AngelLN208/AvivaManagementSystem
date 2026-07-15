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
        retry: false, // Si da 404, significa que recepción no llenó el triaje aún
    });

    // 2. Mutación para guardar la consulta (RN-31, RN-33)
    const mutationConsulta = useMutation({
        mutationFn: (payload) => registrarConsulta(appointmentId, payload),
        onSuccess: () => {
        // Al guardar con éxito, regresamos a la agenda
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
        <div className="max-w-7xl mx-auto h-full flex flex-col">
        {/* Cabecera */}
        <header className="mb-8 flex justify-between items-end">
            <div>
            <h2 className="text-3xl font-light text-[#D4AF37] tracking-wide">Sala de Atención</h2>
            <p className="text-[#D4AF37]/60 font-light mt-1 text-sm tracking-widest">
                CITA #{appointmentId}
            </p>
            </div>
            <button 
            onClick={() => navigate('/doctor/citas')}
            className="px-4 py-2 border-[0.5px] border-[#D4AF37]/50 text-[#D4AF37] hover:bg-[#D4AF37]/10 transition-colors rounded-sm font-light text-sm"
            >
            Volver a la Agenda
            </button>
        </header>

        <div className="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            {/* PANEL IZQUIERDO: Triaje e Información (1 columna) */}
            <div className="lg:col-span-1 space-y-6">
            <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-6 shadow-[0_4px_30px_rgba(0,0,0,0.5)]">
                <h3 className="text-[#D4AF37] font-light text-lg mb-4 border-b-[0.5px] border-[#D4AF37]/20 pb-2 tracking-wide uppercase">
                Signos Vitales
                </h3>
                
                {isLoadingTriaje ? (
                <p className="text-[#D4AF37]/50 font-light text-sm animate-pulse">Recopilando datos...</p>
                ) : !triaje ? (
                <p className="text-gray-400 font-light text-sm">No se registró triaje para esta cita.</p>
                ) : (
                <div className="space-y-4">
                    <div className="flex justify-between items-center border-b-[0.5px] border-[#D4AF37]/10 pb-2">
                    <span className="text-gray-400 font-light text-sm">Presión Arterial</span>
                    <span className="text-gray-200 font-light">{triaje.systolicPressure}/{triaje.diastolicPressure} mmHg</span>
                    </div>
                    <div className="flex justify-between items-center border-b-[0.5px] border-[#D4AF37]/10 pb-2">
                    <span className="text-gray-400 font-light text-sm">Temperatura</span>
                    <span className="text-gray-200 font-light">{triaje.temperature} °C</span>
                    </div>
                    <div className="flex justify-between items-center border-b-[0.5px] border-[#D4AF37]/10 pb-2">
                    <span className="text-gray-400 font-light text-sm">Frec. Cardíaca</span>
                    <span className="text-gray-200 font-light">{triaje.heartRate} lpm</span>
                    </div>
                    <div className="flex justify-between items-center border-b-[0.5px] border-[#D4AF37]/10 pb-2">
                    <span className="text-gray-400 font-light text-sm">Frec. Respiratoria</span>
                    <span className="text-gray-200 font-light">{triaje.respiratoryRate} rpm</span>
                    </div>
                    <div className="flex justify-between items-center pb-2">
                    <span className="text-gray-400 font-light text-sm">Peso / Altura</span>
                    <span className="text-gray-200 font-light">{triaje.weight} kg / {triaje.height} cm</span>
                    </div>
                </div>
                )}
            </div>
            </div>

            {/* PANEL DERECHO: Formulario de Consulta (2 columnas) */}
            <div className="lg:col-span-2">
            <form 
                onSubmit={handleSubmit}
                className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-8 shadow-[0_4px_30px_rgba(0,0,0,0.5)] h-full flex flex-col"
            >
                <h3 className="text-[#D4AF37] font-light text-lg mb-6 border-b-[0.5px] border-[#D4AF37]/20 pb-2 tracking-wide uppercase">
                Registro Clínico
                </h3>

                <div className="space-y-6 flex-1">
                {/* Diagnóstico */}
                <div>
                    <label className="block text-[#D4AF37]/80 font-light text-sm mb-2 tracking-wide">
                    Diagnóstico (CIE-10 o Descripción) *
                    </label>
                    <textarea
                    required
                    rows="3"
                    value={diagnostico}
                    onChange={(e) => setDiagnostico(e.target.value)}
                    className="w-full bg-black/50 border-[0.5px] border-[#D4AF37]/30 text-gray-200 focus:border-[#D4AF37] focus:ring-1 focus:ring-[#D4AF37]/50 rounded-sm font-light p-3 outline-none transition-all resize-none"
                    placeholder="Ingrese el diagnóstico clínico..."
                    />
                </div>

                {/* Tratamiento / Receta */}
                <div>
                    <label className="block text-[#D4AF37]/80 font-light text-sm mb-2 tracking-wide">
                    Tratamiento y Receta Médica *
                    </label>
                    <textarea
                    required
                    rows="4"
                    value={tratamiento}
                    onChange={(e) => setTratamiento(e.target.value)}
                    className="w-full bg-black/50 border-[0.5px] border-[#D4AF37]/30 text-gray-200 focus:border-[#D4AF37] focus:ring-1 focus:ring-[#D4AF37]/50 rounded-sm font-light p-3 outline-none transition-all resize-none"
                    placeholder="Describa los medicamentos, dosis y duración..."
                    />
                </div>

                {/* Notas Adicionales */}
                <div>
                    <label className="block text-[#D4AF37]/80 font-light text-sm mb-2 tracking-wide">
                    Notas Adicionales (Opcional)
                    </label>
                    <textarea
                    rows="2"
                    value={notasAdicionales}
                    onChange={(e) => setNotasAdicionales(e.target.value)}
                    className="w-full bg-black/50 border-[0.5px] border-[#D4AF37]/30 text-gray-200 focus:border-[#D4AF37] focus:ring-1 focus:ring-[#D4AF37]/50 rounded-sm font-light p-3 outline-none transition-all resize-none"
                    placeholder="Observaciones internas, recomendaciones de descanso..."
                    />
                </div>
                </div>

                {/* Botón de Envío */}
                <div className="mt-8 pt-6 border-t-[0.5px] border-[#D4AF37]/20 flex justify-end">
                <button
                    type="submit"
                    disabled={mutationConsulta.isPending}
                    className="px-8 py-3 border-[0.5px] border-[#D4AF37] text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black disabled:opacity-50 disabled:hover:bg-transparent disabled:hover:text-[#D4AF37] transition-all duration-300 font-light text-sm tracking-widest rounded-sm shadow-[0_0_15px_rgba(212,175,55,0.15)] uppercase"
                >
                    {mutationConsulta.isPending ? 'Guardando...' : 'Finalizar Consulta'}
                </button>
                </div>
            </form>
            </div>

        </div>
        </div>
    );
}