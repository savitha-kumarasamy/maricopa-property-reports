import api from './client';
import type { Schedule } from '../types';

export async function listSchedules(): Promise<Schedule[]> {
  const response = await api.get<Schedule[]>('/schedules');
  return response.data;
}

export async function createSchedule(propertyId: string, frequency: string): Promise<Schedule> {
  const response = await api.post<Schedule>('/schedules', {
    property_id: propertyId,
    frequency,
  });
  return response.data;
}

export async function updateSchedule(
  id: string,
  data: { frequency?: string; is_active?: boolean }
): Promise<Schedule> {
  const response = await api.put<Schedule>(`/schedules/${id}`, data);
  return response.data;
}

export async function deleteSchedule(id: string): Promise<void> {
  await api.delete(`/schedules/${id}`);
}
