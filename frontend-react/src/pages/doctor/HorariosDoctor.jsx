import { useQuery } from '@tanstack/react-query';
import { useContext } from 'react';
import AuthContext from '../../context/authContext';
import { getHorariosDeDoctor } from '../../api/horariosApi';

// Diccionario para traducir y ordenar los días de la semana
const DIAS_SEMANA = {
    MONDAY: { nombre: 'Lunes', orden: 1 },
    TUESDAY: { nombre: 'Martes', orden: 2 },
    WEDNESDAY: { nombre: 'Miércoles', orden: 3 },
    THURSDAY: { nombre: 'Jueves', orden: 4 },
    FRIDAY: { nombre: 'Viernes', orden: 5 },
    SATURDAY: { nombre: 'Sábado', orden: 6 },
    SUNDAY: { nombre: 'Domingo', orden: 7 },
    };

    export default function HorariosDoctor() {
    const { usuario } = useContext(AuthContext);
    
    // Extraemos el ID del doctor del usuario autenticado (ajusta el fallback según tu estructura)
    const doctorId = usuario?.id || 1;

    const { data, isLoading, error } = useQuery({
        queryKey: ['horariosDoctor', doctorId],
        queryFn: () => getHorariosDeDoctor(doctorId),
    });

    if (isLoading) {
        return <div className="text-[#D4AF37] font-light animate-pulse">Cargando turnos de trabajo...</div>;
    }

    if (error) {
        return (
        <div className="text-red-400 font-light border-[0.5px] border-red-500/50 p-4 bg-black/40 rounded-sm">
            No se pudieron cargar los horarios asignados.
        </div>
        );
    }

    const horariosRaw = data?.data?.data || [];

    // Ordenar los horarios de Lunes a Domingo
    const horarios = [...horariosRaw].sort((a, b) => {
        const ordenA = DIAS_SEMANA[a.dayOfWeek]?.orden || 99;
        const ordenB = DIAS_SEMANA[b.dayOfWeek]?.orden || 99;
        return ordenA - ordenB;
    });

    return (
        <div className="max-w-5xl mx-auto">
        <header className="mb-8">
            <h2 className="text-3xl font-light text-[#D4AF37] tracking-wide">Horario Laboral Asignado</h2>
            <p className="text-gray-400 font-light mt-1 text-sm">
            Estos son tus días y horas de guardia habilitados para la recepción de citas en la clínica (RN-37).
            </p>
        </header>

        <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-8 shadow-[0_4px_30px_rgba(0,0,0,0.5)]">
            {horarios.length === 0 ? (
            <div className="text-center py-12 text-gray-400 font-light text-sm border-[0.5px] border-[#D4AF37]/10 rounded-sm">
                No tienes turnos de trabajo asignados actualmente. Contacta a administración.
            </div>
            ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {horarios.map((horario) => {
                const diaInfo = DIAS_SEMANA[horario.dayOfWeek] || { nombre: horario.dayOfWeek };
                
                // Formatear horas si vienen en formato HH:mm:ss
                const inicio = horario.startTime ? horario.startTime.slice(0, 5) : '--:--';
                const fin = horario.endTime ? horario.endTime.slice(0, 5) : '--:--';

                return (
                    <div 
                    key={horario.id || `${horario.dayOfWeek}-${horario.startTime}`}
                    className="border-[0.5px] border-[#D4AF37]/20 p-5 rounded-sm bg-black/30 hover:border-[#D4AF37]/60 transition-all duration-300 flex items-center justify-between group"
                    >
                    <div className="flex items-center space-x-4">
                        {/* Indicador visual dorado */}
                        <div className="w-1.5 h-10 bg-[#D4AF37]/40 group-hover:bg-[#D4AF37] transition-colors rounded-full"></div>
                        <div>
                        <h3 className="text-lg font-light text-gray-200 tracking-wide">
                            {diaInfo.nombre}
                        </h3>
                        <p className="text-xs text-[#D4AF37]/70 font-light tracking-widest uppercase mt-0.5">
                            Guardia Activa
                        </p>
                        </div>
                    </div>

                    <div className="text-right">
                        <span className="text-xl font-light text-[#D4AF37] tracking-wider block">
                        {inicio} - {fin}
                        </span>
                        <span className="text-[10px] text-gray-500 font-light uppercase tracking-widest">
                        Hora estándar
                        </span>
                    </div>
                    </div>
                );
                })}
            </div>
            )}

            {/* Nota aclaratoria al pie del contenedor */}
            <div className="mt-8 pt-4 border-t-[0.5px] border-[#D4AF37]/10 flex items-center justify-between text-xs text-gray-400 font-light">
            <span>* La disponibilidad para citas descuenta automáticamente los turnos ya reservados.</span>
            <span className="text-[#D4AF37]/60">Sincronizado con recepción</span>
            </div>
        </div>
        </div>
    );
}