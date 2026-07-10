import { ESTADO_MAP } from '../../utils/formatters';

export default function BadgeEstado({ estado }) {
  const info = ESTADO_MAP[estado] || { clase: 'fondo-info-sutil texto-info', label: estado };
  return (
    <span className={`badge ${info.clase} rounded-pill px-3 py-1`}>
      {info.label}
    </span>
  );
}