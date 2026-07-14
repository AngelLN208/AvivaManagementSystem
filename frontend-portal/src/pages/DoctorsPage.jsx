import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import { useDoctors, useSpecialties } from '../hooks/useCatalogs.js';
import { doctorDisplayName } from '../utils/appointments.js';

export default function DoctorsPage() {
  const doctorsQuery = useDoctors();
  const specialtiesQuery = useSpecialties();
  const [search, setSearch] = useState('');
  const [specialtyId, setSpecialtyId] = useState('');

  const filteredDoctors = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase('es');
    return (doctorsQuery.data || []).filter((doctor) => {
      const fullName = `${doctor.firstName || ''} ${doctor.lastName || ''}`.toLocaleLowerCase('es');
      const matchesSearch = !normalizedSearch || fullName.includes(normalizedSearch);
      const matchesSpecialty = !specialtyId || String(doctor.specialty?.id) === specialtyId;
      return matchesSearch && matchesSpecialty;
    });
  }, [doctorsQuery.data, search, specialtyId]);

  if (doctorsQuery.isLoading || specialtiesQuery.isLoading) {
    return <LoadingState message="Consultando especialistas…" />;
  }
  if (doctorsQuery.isError || specialtiesQuery.isError) {
    const query = doctorsQuery.isError ? doctorsQuery : specialtiesQuery;
    return <ErrorState message={query.error.message} onRetry={query.refetch} />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Equipo Aviva"
        title="Nuestros médicos"
        description="Encuentra profesionales activos por nombre o especialidad."
        action={<Link to="/agendar" className="button button--primary">Agendar cita</Link>}
      />

      <section className="section-card catalog-filters" aria-label="Filtros de médicos">
        <div className="form-field">
          <label htmlFor="doctor-search">Buscar por nombre</label>
          <input
            id="doctor-search"
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Ej. García"
          />
        </div>
        <div className="form-field">
          <label htmlFor="specialty-filter">Especialidad</label>
          <select id="specialty-filter" value={specialtyId} onChange={(event) => setSpecialtyId(event.target.value)}>
            <option value="">Todas las especialidades</option>
            {specialtiesQuery.data.map((specialty) => (
              <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
            ))}
          </select>
        </div>
        <p>{filteredDoctors.length} {filteredDoctors.length === 1 ? 'médico encontrado' : 'médicos encontrados'}</p>
      </section>

      {filteredDoctors.length === 0 ? (
        <section className="state-card state-card--empty">
          <span className="state-card__symbol" aria-hidden="true">?</span>
          <h2>No encontramos coincidencias</h2>
          <p>Prueba con otro nombre o selecciona todas las especialidades.</p>
          <button type="button" className="button button--secondary" onClick={() => { setSearch(''); setSpecialtyId(''); }}>Limpiar filtros</button>
        </section>
      ) : (
        <section className="doctor-catalog" aria-label="Listado de médicos">
          {filteredDoctors.map((doctor) => (
            <article key={doctor.id} className="doctor-card">
              <div className="doctor-card__avatar" aria-hidden="true">{doctor.firstName?.[0]}{doctor.lastName?.[0]}</div>
              <div className="doctor-card__body">
                <span className="doctor-card__specialty">{doctor.specialty?.name || 'Medicina general'}</span>
                <h2>{doctorDisplayName(doctor)}</h2>
                {doctor.specialty?.description && <p>{doctor.specialty.description}</p>}
                {doctor.licenseNumber && <small>Colegiatura: {doctor.licenseNumber}</small>}
              </div>
              <Link
                to={`/agendar?especialidad=${doctor.specialty?.id || ''}&medico=${doctor.id}`}
                className="button button--secondary button--wide"
                aria-label={`Ver disponibilidad de ${doctorDisplayName(doctor)}`}
              >
                Ver disponibilidad
              </Link>
            </article>
          ))}
        </section>
      )}
    </div>
  );
}
