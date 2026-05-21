import api from './client';
import type { Property, PropertySearchResult } from '../types';

export async function listProperties(): Promise<Property[]> {
  const response = await api.get<Property[]>('/properties');
  return response.data;
}

export async function getProperty(id: string): Promise<Property> {
  const response = await api.get<Property>(`/properties/${id}`);
  return response.data;
}

export async function addProperty(apn: string): Promise<Property> {
  const response = await api.post<Property>('/properties', { apn });
  return response.data;
}

export async function deleteProperty(id: string): Promise<void> {
  await api.delete(`/properties/${id}`);
}

export async function syncProperty(id: string): Promise<Property> {
  const response = await api.post<Property>(`/properties/${id}/sync`);
  return response.data;
}

export async function searchProperties(query: string, page = 1): Promise<{
  results: PropertySearchResult[];
  total: number;
  page: number;
}> {
  const response = await api.get('/properties/search', { params: { q: query, page } });
  return response.data;
}
