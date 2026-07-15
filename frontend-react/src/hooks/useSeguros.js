import { useQuery } from '@tanstack/react-query';
import { getSeguros } from '../api/segurosApi';

export function useSeguros() {
  return useQuery({ queryKey: ['seguros'], queryFn: getSeguros });
}