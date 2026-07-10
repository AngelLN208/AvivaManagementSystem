import { useState, useEffect } from 'react';

const NOTIFS_INICIALES = [
  { id:1, leida:false, tipo:'urgente', icono:'fa-circle-exclamation', colorBg:'#fee2e2', colorIcon:'#dc2626', titulo:'Cita urgente sin confirmar', mensaje:'Ana García tiene una cita sin confirmar.', tiempo:'Hace 5 min' },
  { id:2, leida:false, tipo:'info', icono:'fa-calendar-check', colorBg:'#cffafe', colorIcon:'#0891b2', titulo:'Nueva cita agendada', mensaje:'Roberto Chávez registró una cita para mañana.', tiempo:'Hace 20 min' },
  { id:3, leida:false, tipo:'pago', icono:'fa-money-bill-wave', colorBg:'#d1fae5', colorIcon:'#059669', titulo:'Pago pendiente', mensaje:'Carlos Mendoza tiene un pago pendiente.', tiempo:'Hace 1 hora' },
];

export function useNotificaciones() {
  const [notificaciones, setNotificaciones] = useState(() => {
    const guardado = localStorage.getItem('aviva_notificaciones');
    return guardado ? JSON.parse(guardado) : NOTIFS_INICIALES;
  });

  useEffect(() => {
    localStorage.setItem('aviva_notificaciones', JSON.stringify(notificaciones));
  }, [notificaciones]);

  const marcarLeida = (id) => {
    setNotificaciones(prev => prev.map(n => (n.id === id ? { ...n, leida: true } : n)));
  };

  const marcarTodasLeidas = () => {
    setNotificaciones(prev => prev.map(n => ({ ...n, leida: true })));
  };

  const noLeidas = notificaciones.filter(n => !n.leida);

  return { notificaciones, noLeidas, marcarLeida, marcarTodasLeidas };
}