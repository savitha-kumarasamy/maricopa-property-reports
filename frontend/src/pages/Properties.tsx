import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listProperties, deleteProperty } from '../api/properties';
import type { Property } from '../types';

export default function Properties() {
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchProperties = () => {
    setLoading(true);
    listProperties()
      .then(setProperties)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchProperties();
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to remove this property?')) return;
    await deleteProperty(id);
    fetchProperties();
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Your Properties</h1>
        <Link
          to="/properties/add"
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium"
        >
          Add Property
        </Link>
      </div>

      {properties.length === 0 ? (
        <div className="bg-white rounded-xl shadow p-12 text-center">
          <p className="text-gray-500 text-lg">No properties tracked yet.</p>
          <Link
            to="/properties/add"
            className="mt-4 inline-block px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium"
          >
            Add your first property
          </Link>
        </div>
      ) : (
        <div className="grid gap-4">
          {properties.map((prop) => (
            <div key={prop.id} className="bg-white rounded-xl shadow p-6">
              <div className="flex justify-between items-start">
                <div>
                  <Link
                    to={`/properties/${prop.id}`}
                    className="text-lg font-semibold text-blue-600 hover:text-blue-500"
                  >
                    APN: {prop.apn}
                  </Link>
                  <p className="mt-1 text-gray-600">{prop.property_address || 'Address not available'}</p>
                  <div className="mt-2 flex gap-4 text-sm text-gray-500">
                    {prop.owner_name && <span>Owner: {prop.owner_name}</span>}
                    {prop.parcel_type && <span>Type: {prop.parcel_type}</span>}
                    {prop.last_synced_at && (
                      <span>Last synced: {new Date(prop.last_synced_at).toLocaleDateString()}</span>
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  <Link
                    to={`/properties/${prop.id}`}
                    className="px-3 py-1 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg"
                  >
                    View
                  </Link>
                  <button
                    onClick={() => handleDelete(prop.id)}
                    className="px-3 py-1 text-sm bg-red-100 hover:bg-red-200 text-red-700 rounded-lg"
                  >
                    Remove
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
