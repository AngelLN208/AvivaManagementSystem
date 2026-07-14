import { useMemo, useState } from 'react';
import { Search, Stethoscope } from 'lucide-react';
import { Link } from 'react-router-dom';
import { ErrorState, LoadingState } from '../components/ui/AsyncState.jsx';
import PageHeader from '../components/ui/PageHeader.jsx';
import { Button } from '../components/ui/button.jsx';
import { Card, CardContent } from '../components/ui/card.jsx';
import { Input } from '../components/ui/input.jsx';
import { Label } from '../components/ui/label.jsx';
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
    <div className="space-y-6">
      <PageHeader
        eyebrow="Equipo Aviva"
        title="Nuestros médicos"
        description="Encuentra profesionales activos por nombre o especialidad."
        action={<Button asChild><Link to="/agendar">Agendar cita</Link></Button>}
      />

      <Card aria-label="Filtros de médicos">
        <CardContent className="grid gap-4 pt-0 md:grid-cols-[1fr_1fr_auto] md:items-end">
        <div className="space-y-2">
          <Label htmlFor="doctor-search">Buscar por nombre</Label>
          <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          <Input
            id="doctor-search"
            type="search"
            className="pl-9"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Ej. García"
          />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="specialty-filter">Especialidad</Label>
          <select className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring/35" id="specialty-filter" value={specialtyId} onChange={(event) => setSpecialtyId(event.target.value)}>
            <option value="">Todas las especialidades</option>
            {specialtiesQuery.data.map((specialty) => (
              <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
            ))}
          </select>
        </div>
        <p className="m-0 pb-2 text-sm font-semibold text-muted-foreground" aria-live="polite">{filteredDoctors.length} {filteredDoctors.length === 1 ? 'médico encontrado' : 'médicos encontrados'}</p>
        </CardContent>
      </Card>

      {filteredDoctors.length === 0 ? (
        <section className="flex min-h-64 flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-muted/35 px-6 text-center">
          <span className="mb-4 flex size-12 items-center justify-center rounded-full bg-background text-primary shadow-sm ring-1 ring-border"><Search className="size-5" aria-hidden="true" /></span>
          <h2 className="m-0 text-xl font-semibold">No encontramos coincidencias</h2>
          <p className="mb-5 mt-2 text-sm text-muted-foreground">Prueba con otro nombre o selecciona todas las especialidades.</p>
          <Button type="button" variant="outline" onClick={() => { setSearch(''); setSpecialtyId(''); }}>Limpiar filtros</Button>
        </section>
      ) : (
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3" aria-label="Listado de médicos">
          {filteredDoctors.map((doctor) => (
            <Card key={doctor.id} className="gap-5 transition hover:-translate-y-0.5 hover:border-primary/25 hover:shadow-md">
              <CardContent className="flex flex-1 flex-col pt-0">
              <div className="mb-5 flex items-center gap-3">
                <div className="flex size-12 items-center justify-center rounded-xl bg-primary/10 font-bold text-primary" aria-hidden="true">{doctor.firstName?.[0]}{doctor.lastName?.[0]}</div>
                <span className="inline-flex items-center gap-1.5 rounded-full bg-primary/8 px-2.5 py-1 text-xs font-bold text-primary"><Stethoscope className="size-3.5" />{doctor.specialty?.name || 'Medicina general'}</span>
              </div>
              <div className="flex-1">
                <h2 className="m-0 text-lg font-semibold">{doctorDisplayName(doctor)}</h2>
                {doctor.specialty?.description && <p className="mb-0 mt-2 text-sm leading-6 text-muted-foreground">{doctor.specialty.description}</p>}
                {doctor.licenseNumber && <small className="mt-3 block text-xs font-semibold text-muted-foreground">Colegiatura: {doctor.licenseNumber}</small>}
              </div>
              <Button
                asChild
                variant="outline"
                className="mt-5 w-full"
              >
              <Link
                to={`/agendar?especialidad=${doctor.specialty?.id || ''}&medico=${doctor.id}`}
                aria-label={`Ver disponibilidad de ${doctorDisplayName(doctor)}`}
              >
                Ver disponibilidad
              </Link>
              </Button>
              </CardContent>
            </Card>
          ))}
        </section>
      )}
    </div>
  );
}
