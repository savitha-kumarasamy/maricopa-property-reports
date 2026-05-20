import api from './client';
import type { TokenResponse, User } from '../types';

export async function register(email: string, password: string, full_name: string): Promise<User> {
  const response = await api.post<User>('/auth/register', { email, password, full_name });
  return response.data;
}

export async function login(email: string, password: string): Promise<TokenResponse> {
  const response = await api.post<TokenResponse>('/auth/login', { email, password });
  return response.data;
}

export async function getMe(): Promise<User> {
  const response = await api.get<User>('/auth/me');
  return response.data;
}
