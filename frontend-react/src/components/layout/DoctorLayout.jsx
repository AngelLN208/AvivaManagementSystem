import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import AuthContext from '../../context/authContext';

export default function DoctorLayout() {
    const { usuario, logout } = useContext(AuthContext);
    const location = useLocation();
    const navigate = useNavigate();

    return (
        <div 
        className="min-h-screen bg-cover bg-center flex font-sans"
        style={{ 
            backgroundImage: 'url("/fondo-clinica.jpg")', // Asegúrate de tener este escenario guardado en /public
            backgroundColor: '#0a0a0a' 
        }}
        >
        {/* Overlay translúcido para legibilidad */}
        <div className="absolute inset-0 bg-black/75 backdrop-blur-sm z-0"></div>

        {/* Sidebar */}
        <aside className="relative z-10 w-64 bg-black/40 border-r-[0.5px] border-[#D4AF37]/30 flex flex-col shadow-[4px_0_24px_rgba(0,0,0,0.6)]">
            <div className="p-6 border-b-[0.5px] border-[#D4AF37]/20 text-center">
            <h1 className="text-[#D4AF37] text-2xl font-thin tracking-[0.2em] uppercase">
                Aviva <span className="font-normal">Clinic</span>
            </h1>
            <p className="text-[#D4AF37]/50 text-[10px] mt-2 tracking-widest uppercase">Panel Médico</p>
            </div>

            <nav className="flex-1 px-4 py-8 space-y-3">
            <Link 
                to="/doctor/citas"
                className={`flex items-center px-4 py-3 rounded-sm border-[0.5px] transition-all duration-300 text-sm font-light tracking-wide ${
                location.pathname.includes('/doctor/citas') && !location.pathname.includes('/atender')
                    ? 'bg-[#D4AF37]/10 border-[#D4AF37]/80 text-[#D4AF37] shadow-[0_0_15px_rgba(212,175,55,0.15)]' 
                    : 'border-transparent text-gray-400 hover:border-[#D4AF37]/40 hover:text-[#D4AF37]/80'
                }`}
            >
                <span className="mr-3">◇</span> Visión General
            </Link>
            <Link 
                to="/doctor/horarios"
                className={`flex items-center px-4 py-3 rounded-sm border-[0.5px] transition-all duration-300 text-sm font-light tracking-wide ${
                location.pathname.includes('/doctor/horarios') 
                    ? 'bg-[#D4AF37]/10 border-[#D4AF37]/80 text-[#D4AF37] shadow-[0_0_15px_rgba(212,175,55,0.15)]' 
                    : 'border-transparent text-gray-400 hover:border-[#D4AF37]/40 hover:text-[#D4AF37]/80'
                }`}
            >
                <span className="mr-3">◇</span> Calendario
            </Link>
            </nav>

            <div className="p-6 border-t-[0.5px] border-[#D4AF37]/20">
            <button 
                onClick={logout}
                className="w-full py-2.5 px-4 border-[0.5px] border-red-500/30 text-red-400/80 hover:bg-red-500/10 hover:border-red-400/80 transition-colors rounded-sm font-light text-xs tracking-widest uppercase"
            >
                Cerrar Sesión
            </button>
            </div>
        </aside>

        {/* Contenido Principal */}
        <div className="relative z-10 flex-1 flex flex-col h-screen overflow-hidden">
            {/* Top Navbar */}
            <header className="h-20 border-b-[0.5px] border-[#D4AF37]/20 bg-black/20 backdrop-blur-md flex items-center justify-between px-8">
            <div className="flex items-center w-96">
                <div className="relative w-full">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-[#D4AF37]/50 text-sm">⚲</span>
                <input 
                    type="text" 
                    placeholder="Buscar paciente o cita..." 
                    className="w-full bg-black/40 border-[0.5px] border-[#D4AF37]/20 text-gray-200 text-sm font-light rounded-sm pl-10 pr-4 py-2 outline-none focus:border-[#D4AF37]/60 transition-colors placeholder:text-gray-600"
                />
                </div>
            </div>
            <div className="flex items-center gap-4 border-l-[0.5px] border-[#D4AF37]/20 pl-6">
                <div className="text-right">
                <p className="text-[#D4AF37] text-sm font-light tracking-wide">Dr. {usuario?.username || 'Emile Chen'}</p>
                <p className="text-gray-500 text-[10px] tracking-widest uppercase">Medicina General</p>
                </div>
                <div className="w-10 h-10 rounded-sm border-[0.5px] border-[#D4AF37]/50 p-0.5">
                <div className="w-full h-full bg-[#D4AF37]/20 rounded-sm"></div>
                </div>
            </div>
            </header>

            {/* Renderizado de vistas */}
            <main className="flex-1 overflow-y-auto p-8">
            <Outlet />
            </main>
        </div>
        </div>
    );
}