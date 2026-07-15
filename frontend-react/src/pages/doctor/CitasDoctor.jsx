import { useNavigate } from 'react-router-dom';

export default function CitasDoctor() {
    const navigate = useNavigate();

    // Datos ficticios extraídos de tu HTML
    const citasMock = [
        { id: 1, hora: '08:00 AM', paciente: 'Carlos Mendoza', motivo: 'Chequeo general', estado: 'Atendida' },
        { id: 2, hora: '09:00 AM', paciente: 'María González', motivo: 'Dolor de cabeza persistente', estado: 'En Espera' },
        { id: 3, hora: '10:00 AM', paciente: 'Luis Ramírez', motivo: 'Revisión de exámenes', estado: 'No llega' },
        { id: 4, hora: '10:30 AM', paciente: 'Ana Sofía Vargas', motivo: 'Fiebre alta', estado: 'Adicional' },
    ];

    return (
        <div className="max-w-6xl mx-auto animation-fade-in">
        <div className="mb-8">
            <h2 className="text-2xl font-thin text-[#D4AF37] tracking-[0.1em] uppercase">Visión General</h2>
        </div>

        {/* Tarjeta de Estadísticas */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 p-6 rounded-sm flex justify-between items-start shadow-[0_4px_20px_rgba(0,0,0,0.4)]">
            <div>
                <p className="text-gray-400 font-light text-xs tracking-widest uppercase mb-2">Total Pacientes</p>
                <h3 className="text-3xl font-thin text-[#D4AF37]">24,839</h3>
                <p className="text-[#D4AF37]/60 text-[10px] mt-2 tracking-wide">+7.8% vs semana pasada</p>
            </div>
            <div className="text-[#D4AF37]/40 text-2xl border-[0.5px] border-[#D4AF37]/20 p-3 rounded-sm bg-[#D4AF37]/5">
                ☖
            </div>
            </div>
        </div>

        {/* Tabla de Citas */}
        <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-8 shadow-[0_4px_20px_rgba(0,0,0,0.4)]">
            <h5 className="text-lg font-light text-[#D4AF37] mb-6 tracking-wide border-b-[0.5px] border-[#D4AF37]/20 pb-4">
            CITAS DEL DÍA
            </h5>
            <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
                <thead>
                <tr className="border-b-[0.5px] border-[#D4AF37]/20">
                    <th className="py-3 px-4 text-[#D4AF37]/70 font-light text-[11px] uppercase tracking-widest">Hora</th>
                    <th className="py-3 px-4 text-[#D4AF37]/70 font-light text-[11px] uppercase tracking-widest">Paciente</th>
                    <th className="py-3 px-4 text-[#D4AF37]/70 font-light text-[11px] uppercase tracking-widest">Motivo</th>
                    <th className="py-3 px-4 text-[#D4AF37]/70 font-light text-[11px] uppercase tracking-widest">Estado</th>
                    <th className="py-3 px-4 text-[#D4AF37]/70 font-light text-[11px] uppercase tracking-widest text-right">Acción</th>
                </tr>
                </thead>
                <tbody>
                {citasMock.map((cita) => (
                    <tr key={cita.id} className="border-b-[0.5px] border-[#D4AF37]/10 hover:bg-[#D4AF37]/5 transition-colors">
                    <td className="py-4 px-4 text-gray-300 font-light text-sm">{cita.hora}</td>
                    <td className="py-4 px-4 text-gray-200 font-light text-sm">{cita.paciente}</td>
                    <td className="py-4 px-4 text-gray-400 font-light text-sm">{cita.motivo}</td>
                    <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-sm text-[10px] font-light border-[0.5px] tracking-wider uppercase ${
                        cita.estado === 'Atendida' ? 'border-[#D4AF37]/50 text-[#D4AF37] bg-[#D4AF37]/10' :
                        cita.estado === 'En Espera' ? 'border-blue-400/50 text-blue-300 bg-blue-500/10' :
                        cita.estado === 'Adicional' ? 'border-purple-400/50 text-purple-300 bg-purple-500/10' :
                        'border-red-400/50 text-red-300 bg-red-500/10'
                        }`}>
                        {cita.estado}
                        </span>
                    </td>
                    <td className="py-4 px-4 text-right">
                        {cita.estado === 'En Espera' || cita.estado === 'Adicional' ? (
                        <button 
                            onClick={() => navigate(`/doctor/atender/${cita.id}`)}
                            className="px-4 py-1.5 border-[0.5px] border-[#D4AF37] text-[#D4AF37] hover:bg-[#D4AF37] hover:text-black transition-all duration-300 font-light text-xs tracking-widest rounded-sm uppercase"
                        >
                            Iniciar Consulta
                        </button>
                        ) : (
                        <button className="px-4 py-1.5 border-[0.5px] border-gray-600/50 text-gray-500 font-light text-xs tracking-widest rounded-sm uppercase hover:bg-gray-800 transition-colors">
                            Ver Detalle
                        </button>
                        )}
                    </td>
                    </tr>
                ))}
                </tbody>
            </table>
            </div>
        </div>
        </div>
    );
}