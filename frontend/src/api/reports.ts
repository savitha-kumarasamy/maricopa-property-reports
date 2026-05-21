import api from './client';
import type { Report } from '../types';

export async function listReports(): Promise<{ reports: Report[]; total: number }> {
  const response = await api.get('/reports');
  return response.data;
}

export async function getReport(id: string): Promise<Report> {
  const response = await api.get<Report>(`/reports/${id}`);
  return response.data;
}

export async function generateReport(propertyId: string): Promise<Report> {
  const response = await api.post<Report>(`/reports/generate/${propertyId}`);
  return response.data;
}
