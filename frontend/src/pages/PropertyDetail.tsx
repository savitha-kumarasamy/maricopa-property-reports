import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getProperty, syncProperty } from '../api/properties';
import { generateReport } from '../api/reports';
import type { Property } from '../types';

export default function PropertyDetail() {
  const { id } = useParams<{ id: string }>();
  const [property, setProperty] = useState<Property | null>(null);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (id) {
      getProperty(id)
        .then(setProperty)
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleSync = async () => {
    if (!id) return;
    setSyncing(true);
    try {
      const updated = await syncProperty(id);
      setProperty(updated);
      setMessage('Property data synced successfully.');
    } catch {
      setMessage('Failed to sync property data.');
    } finally {
      setSyncing(false);
    }
  };

  const handleGenerate = async () => {
    if (!id) return;
    setGenerating(true);
    try {
      await generateReport(id);
      setMessage('Report generated successfully! View it in Reports.');
    } catch {
      setMessage('Failed to generate report.');
    } finally {
      setGenerating(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  if (!property) {
    return <div className="text-center py-12 text-gray-500">Property not found.</div>;
  }

  const rawData = property.raw_property_data || {};

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex justify-between items-start mb-8">
        <div>
          <Link to="/properties" className="text-sm text-blue-600 hover:text-blue-500 mb-2 inline-block">
            &larr; Back to Properties
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">APN: {property.apn}</h1>
          <p className="mt-1 text-gray-600">{property.property_address || 'Address not available'}</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={handleSync}
            disabled={syncing}
            className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium disabled:opacity-50"
          >
            {syncing ? 'Syncing...' : 'Sync Data'}
          </button>
          <button
            onClick={handleGenerate}
            disabled={generating}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium disabled:opacity-50"
          >
            {generating ? 'Generating...' : 'Generate Report'}
          </button>
        </div>
      </div>

      {message && (
        <div className="mb-6 bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg">
          {message}
        </div>
      )}

      <div className="grid md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Property Information</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm text-gray-500">APN</dt>
              <dd className="font-medium text-gray-900">{property.apn}</dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500">Address</dt>
              <dd className="font-medium text-gray-900">{property.property_address || 'N/A'}</dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500">Owner</dt>
              <dd className="font-medium text-gray-900">{property.owner_name || 'N/A'}</dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500">Parcel Type</dt>
              <dd className="font-medium text-gray-900">{property.parcel_type || 'N/A'}</dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500">Last Synced</dt>
              <dd className="font-medium text-gray-900">
                {property.last_synced_at
                  ? new Date(property.last_synced_at).toLocaleString()
                  : 'Never'}
              </dd>
            </div>
          </dl>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Valuation Data</h2>
          {rawData.valuations && typeof rawData.valuations === 'object' && !('error' in (rawData.valuations as Record<string, unknown>)) ? (
            <pre className="text-sm text-gray-700 bg-gray-50 p-4 rounded-lg overflow-auto max-h-64">
              {JSON.stringify(rawData.valuations, null, 2)}
            </pre>
          ) : (
            <p className="text-gray-500">No valuation data available. Try syncing.</p>
          )}
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Owner Details</h2>
          {rawData.owner && typeof rawData.owner === 'object' && !('error' in (rawData.owner as Record<string, unknown>)) ? (
            <pre className="text-sm text-gray-700 bg-gray-50 p-4 rounded-lg overflow-auto max-h-64">
              {JSON.stringify(rawData.owner, null, 2)}
            </pre>
          ) : (
            <p className="text-gray-500">No owner data available. Try syncing.</p>
          )}
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Parcel Details</h2>
          {rawData.parcel && typeof rawData.parcel === 'object' && !('error' in (rawData.parcel as Record<string, unknown>)) ? (
            <pre className="text-sm text-gray-700 bg-gray-50 p-4 rounded-lg overflow-auto max-h-64">
              {JSON.stringify(rawData.parcel, null, 2)}
            </pre>
          ) : (
            <p className="text-gray-500">No parcel data available. Try syncing.</p>
          )}
        </div>
      </div>
    </div>
  );
}
