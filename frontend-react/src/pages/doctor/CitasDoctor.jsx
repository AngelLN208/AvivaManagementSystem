import { useQuery } from '@tanstack/react-query';
import { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthContext from '../../context/authContext';
import { getCitasDeDoctor } from '../../api/citasApi'; // Asegúrate de haber agregado esta función

export default function CitasDoctor() {
    const { usuario } = useContext(AuthContext);
    const navigate = useNavigate();

    // Asumimos que el backend devuelve el doctorId en el token, o que podemos usar el ID del usuario
    // Si tu backend requiere un doctorId específico numérico, asegúrate de extraerlo del 'usuario'
    const doctorId = usuario?.id || 1; // Ajusta esto según cómo guardes el ID en AuthContext

    const { data, isLoading, error } = useQuery({
        queryKey: ['citasDoctor', doctorId],
        queryFn: () => getCitasDeDoctor(doctorId),
    });

    if (isLoading) return <div className="text-[#D4AF37] font-light animate-pulse">Cargando agenda...</div>;
    if (error) return <div className="text-red-400 font-light border-[0.5px] border-red-500/50 p-4 bg-black/40">Error al cargar las citas.</div>;

    // Extraemos la lista de citas (ajusta según la estructura de tu ApiResponse)
    const citas = data?.data?.data || [];

    return (
        <div className="max-w-6xl mx-auto">
        <header className="mb-8">
            <h2 className="text-3xl font-light text-[#D4AF37] tracking-wide">Citas del Día</h2>
            <p className="text-gray-400 font-light mt-1 text-sm">Gestiona tus consultas y pacientes</p>
        </header>

        {/* Contenedor principal con estética cristal/dorada */}
        <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-6 shadow-[0_4px_30px_rgba(0,0,0,0.5)]">
            
            {citas.length === 0 ? (
            <div className="text-center py-10 text-gray-400 font-light text-sm">
                No tienes citas programadas por el momento.
            </div>
            ) : (
            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                <thead>
                    <tr className="border-b-[0.5px] border-[#D4AF37]/30">
                    <th className="py-3 px-4 text-[#D4AF37] font-light text-sm uppercase tracking-wider">Hora</th>
                    <th className="py-3 px-4 text-[#D4AF37] font-light text-sm uppercase tracking-wider">Paciente</th>
                    <th className="py-3 px-4 text-[#D4AF37] font-light text-sm uppercase tracking-wider">Motivo</th>
                    <th className="py-3 px-4 text-[#D4AF37] font-light text-sm uppercase tracking-wider">Estado</th>
                    <th className="py-3 px-4 text-right text-[#D4AF37] font-light text-sm uppercase tracking-wider">Acción</th>
                    </tr>
                </thead>
                <tbody>
                    {citas.map((cita) => (
                    <tr 
                        key={cita.id} 
                        className="border-b-[0.5px] border-[#D4AF37]/10 hover:bg-[#D4AF37]/5 transition-colors"
                    >
                        <td className="py-4 px-4 text-gray-200 font-light text-sm">
                        {new Date(cita.appointmentDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </td>
                        <td className="py-4 px-4 text-gray-200 font-light text-sm">
                        {cita.patient?.firstName} {cita.patient?.lastName}
                        </td>
                        <td className="py-4 px-4 text-gray-400 font-light text-sm">
                        {cita.reason || 'Consulta General'}
                        </td>
                        <td className="py-4 px-4">
                        {/* Badge de estado con trazos finos */}
                        <span className={`px-3 py-1 rounded-full text-xs font-light border-[0.5px] ${
                            cita.status === 'CONFIRMED' ? 'border-green-500/50 text-green-400 bg-green-500/10' :
                            cita.status === 'PENDING' ? 'border-yellow-500/50 text-yellow-400 bg-yellow-500/10' :
                            cita.status === 'COMPLETED' ? 'border-blue-500/50 text-blue-400 bg-blue-500/10' :
                            'border-gray-500/50 text-gray-400 bg-gray-500/10'
                        }`}>
                            {cita.status}
                        </span>
                        </td>
                        <td className="py-4 px-4 text-right">
                        {cita.status === 'CONFIRMED' ? (
                            <button
                            onClick={() => navigate(`/doctor/atender/${cita.id}`)}
                            className="px-4 py-1.5 border-[0.5px] border-[#D4AF37] text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black transition-all duration-300 font-light text-sm tracking-wide rounded-sm shadow-[0_0_8px_rgba(212,175,55,0.2)]"
                            >
                            Atender
                            </button>
                        ) : (
                            <button
                            disabled
                            className="px-4 py-1.5 border-[0.5px] border-gray-600/50 text-gray-500 font-light text-sm rounded-sm cursor-not-allowed"
                            title="El pago debe estar procesado (CONFIRMED) para atender."
                            >
                            Pendiente
                            </button>
                        )}
                        </td>
                    </tr>
                    ))}
                </tbody>
                </table>
            </div>
            )}
        </div>
        </div>
    );
}