export default function HorariosDoctor() {
    return (
        <div className="max-w-7xl mx-auto animation-fade-in">
        <div className="flex justify-between items-end mb-8">
            <div>
            <h2 className="text-2xl font-thin text-[#D4AF37] tracking-[0.1em] uppercase">Calendario de Citas</h2>
            </div>
            <div className="flex items-center border-[0.5px] border-[#D4AF37]/30 rounded-sm bg-black/40 backdrop-blur-sm">
            <button className="px-4 py-2 text-[#D4AF37]/50 hover:text-[#D4AF37] transition-colors font-light">⟨</button>
            <div className="px-4 py-2 border-l-[0.5px] border-r-[0.5px] border-[#D4AF37]/30 text-[#D4AF37] font-light text-sm tracking-widest">
                13 ABR - 17 ABR, 2026
            </div>
            <button className="px-4 py-2 text-[#D4AF37]/50 hover:text-[#D4AF37] transition-colors font-light">⟩</button>
            </div>
        </div>

        <div className="bg-black/40 backdrop-blur-md border-[0.5px] border-[#D4AF37]/30 rounded-sm p-8 shadow-[0_4px_20px_rgba(0,0,0,0.4)]">
            
            {/* Leyenda */}
            <div className="flex gap-6 mb-6 pb-6 border-b-[0.5px] border-[#D4AF37]/20">
            <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-[#D4AF37] shadow-[0_0_8px_#D4AF37]"></span>
                <span className="text-gray-300 font-light text-xs tracking-widest uppercase">Normal</span>
            </div>
            <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-blue-400 shadow-[0_0_8px_#60a5fa]"></span>
                <span className="text-gray-300 font-light text-xs tracking-widest uppercase">Reprogramada</span>
            </div>
            </div>

            {/* Grid de Calendario Ficticio */}
            <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] border-collapse">
                <thead>
                <tr>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37]/70 font-light text-xs tracking-widest uppercase w-[10%]">Hora</th>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37] font-light text-xs tracking-widest uppercase w-[18%]">Lun 13</th>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37] font-light text-xs tracking-widest uppercase w-[18%]">Mar 14</th>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37] font-light text-xs tracking-widest uppercase w-[18%]">Mié 15</th>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37] font-light text-xs tracking-widest uppercase w-[18%]">Jue 16</th>
                    <th className="py-4 border-[0.5px] border-[#D4AF37]/20 bg-black/20 text-[#D4AF37] font-light text-xs tracking-widest uppercase w-[18%]">Vie 17</th>
                </tr>
                </thead>
                <tbody>
                {/* Fila 08:00 AM */}
                <tr>
                    <td className="p-4 border-[0.5px] border-[#D4AF37]/20 text-center text-gray-400 font-light text-xs tracking-widest">08:00</td>
                    <td className="p-2 border-[0.5px] border-[#D4AF37]/20 align-top">
                    <div className="bg-[#D4AF37]/10 border-l-[2px] border-[#D4AF37] p-3 rounded-r-sm">
                        <p className="text-[#D4AF37] font-normal text-sm">Carlos M.</p>
                        <p className="text-[#D4AF37]/60 text-[10px] uppercase tracking-widest mt-1">Normal</p>
                    </div>
                    </td>
                    <td className="p-2 border-[0.5px] border-[#D4AF37]/20 bg-white/5"></td>
                    <td className="p-2 border-[0.5px] border-[#D4AF37]/20 align-top">
                    <div className="bg-blue-500/10 border-l-[2px] border-blue-400 p-3 rounded-r-sm">
                        <p className="text-blue-300 font-normal text-sm">Ana V.</p>
                        <p className="text-blue-300/60 text-[10px] uppercase tracking-widest mt-1">Resultados</p>
                    </div>
                    </td>
                    <td className="p-2 border-[0.5px] border-[#D4AF37]/20 bg-white/5"></td>
                    <td className="p-2 border-[0.5px] border-[#D4AF37]/20 bg-white/5"></td>
                </tr>
                {/* Fila Descanso */}
                <tr>
                    <td className="p-4 border-[0.5px] border-[#D4AF37]/20 text-center text-gray-400 font-light text-xs tracking-widest">12:00</td>
                    <td colSpan="5" className="p-4 border-[0.5px] border-[#D4AF37]/20 text-center bg-black/60">
                    <span className="text-[#D4AF37]/40 font-light text-xs tracking-[0.3em] uppercase">Horario de Descanso Médico</span>
                    </td>
                </tr>
                </tbody>
            </table>
            </div>
        </div>
        </div>
    );
}