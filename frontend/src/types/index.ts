export interface User {
  id: string;
  email: string;
  full_name: string;
  is_active: boolean;
  created_at: string;
}

export interface Property {
  id: string;
  apn: string;
  property_address: string | null;
  owner_name: string | null;
  parcel_type: string | null;
  last_synced_at: string | null;
  raw_property_data: Record<string, unknown> | null;
  created_at: string;
}

export interface PropertySearchResult {
  apn: string;
  address: string | null;
  owner_name: string | null;
  parcel_type: string | null;
}

export interface Report {
  id: string;
  property_id: string;
  report_type: string;
  generated_at: string;
  sent_at: string | null;
  email_sent_to: string | null;
  status: string;
  report_data: Record<string, unknown> | null;
  pdf_url: string | null;
  created_at: string;
}

export interface Schedule {
  id: string;
  property_id: string;
  frequency: string;
  next_run_at: string;
  is_active: boolean;
  created_at: string;
  updated_at: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
}
