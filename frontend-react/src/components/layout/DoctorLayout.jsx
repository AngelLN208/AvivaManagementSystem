import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useContext } from 'react';
import AuthContext from '../../context/authContext';

export default function DoctorLayout() {
    const { usuario, logout } = useContext(AuthContext);
    const location = useLocation();
    const navigate = useNavigate();

    return (
        // Contenedor principal con la imagen de fondo estilo anime (escenario clínico)
        <div 
        className="min-h-screen bg-cover bg-center flex"
        style={{ 
            // Reemplaza esta URL por tu imagen de escenario clínico estilo anime sin personajes
            backgroundImage: 'url("https://tu-dominio.com/ruta-a-tu-fondo-anime-clinica.jpg")',
            backgroundColor: '#0a0a0a' // Color de respaldo oscuro
        }}
        >
        {/* Capa de superposición para mejorar la legibilidad pero mantener la estética */}
        <div className="absolute inset-0 bg-black/60 backdrop-blur-sm z-0"></div>

        {/* Sidebar - Trazos finos y temática dorada */}
        <aside className="relative z-10 w-64 bg-black/40 border-r-[0.5px] border-[#D4AF37]/40 flex flex-col backdrop-blur-md">
            <div className="p-6 border-b-[0.5px] border-[#D4AF37]/30">
            <h1 className="text-[#D4AF37] text-xl font-light tracking-widest text-center">
                AVIVA <span className="font-bold">CLINIC</span>
            </h1>
            <p className="text-[#D4AF37]/70 text-xs text-center mt-2 font-light">Panel Médico</p>
            </div>

            <nav className="flex-1 px-4 py-6 space-y-2">
            <Link 
                to="/doctor/citas"
                className={`block px-4 py-3 rounded-sm border-[0.5px] transition-all duration-300 font-light ${
                location.pathname.includes('/doctor/citas') 
                    ? 'bg-[#D4AF37]/10 border-[#D4AF37] text-[#D4AF37] shadow-[0_0_10px_rgba(212,175,55,0.2)]' 
                    : 'border-transparent text-gray-300 hover:border-[#D4AF37]/50 hover:text-[#D4AF37]'
                }`}
            >
                Mi Agenda
            </Link>
            {/* Puedes agregar más enlaces aquí en el futuro (ej. Historial) */}
            <Link 
            to="/doctor/horarios"
            className={`block px-4 py-3 rounded-sm border-[0.5px] transition-all duration-300 font-light ${
                location.pathname.includes('/doctor/horarios') 
                ? 'bg-[#D4AF37]/10 border-[#D4AF37] text-[#D4AF37] shadow-[0_0_10px_rgba(212,175,55,0.2)]' 
                : 'border-transparent text-gray-300 hover:border-[#D4AF37]/50 hover:text-[#D4AF37]'
            }`}
            >
            Mis Horarios
            </Link>
            </nav>

            <div className="p-4 border-t-[0.5px] border-[#D4AF37]/30">
            <div className="mb-4 text-center">
                <p className="text-gray-300 text-sm font-light">Dr. {usuario?.username}</p>
            </div>
            <button 
                onClick={logout}
                className="w-full py-2 px-4 border-[0.5px] border-red-500/50 text-red-400 hover:bg-red-500/10 hover:border-red-400 transition-colors rounded-sm font-light text-sm"
            >
                Cerrar Sesión
            </button>
            </div>
        </aside>

        {/* Contenedor Principal de la Vista */}
        <main className="relative z-10 flex-1 p-8 overflow-y-auto">
            {/* Aquí se renderizarán las páginas (CitasDoctor, AtencionClinica) */}
            <Outlet />
        </main>
        </div>
    );
}