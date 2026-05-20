import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { addProperty, searchProperties } from '../api/properties';
import type { PropertySearchResult } from '../types';

export default function AddProperty() {
  const [searchQuery, setSearchQuery] = useState('');
  const [apn, setApn] = useState('');
  const [searchResults, setSearchResults] = useState<PropertySearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSearch = async (e: FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setSearching(true);
    setError('');
    try {
      const data = await searchProperties(searchQuery);
      setSearchResults(data.results);
      if (data.results.length === 0) {
        setError('No properties found. Try a different search or enter the APN directly.');
      }
    } catch {
      setError('Search failed. You can still add a property by APN directly below.');
    } finally {
      setSearching(false);
    }
  };

  const handleAdd = async (selectedApn: string) => {
    setAdding(true);
    setError('');
    try {
      await addProperty(selectedApn);
      navigate('/properties');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { detail?: string } } };
        setError(axiosErr.response?.data?.detail || 'Failed to add property');
      } else {
        setError('Failed to add property');
      }
    } finally {
      setAdding(false);
    }
  };

  const handleDirectAdd = async (e: FormEvent) => {
    e.preventDefault();
    if (!apn.trim()) return;
    await handleAdd(apn.trim());
  };

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Add Property</h1>

      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl shadow p-6 mb-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Search Maricopa County Properties</h2>
        <form onSubmit={handleSearch} className="flex gap-3">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by address, owner name, or APN..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          />
          <button
            type="submit"
            disabled={searching}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium disabled:opacity-50"
          >
            {searching ? 'Searching...' : 'Search'}
          </button>
        </form>

        {searchResults.length > 0 && (
          <div className="mt-6 space-y-3">
            <h3 className="text-sm font-medium text-gray-500">Search Results</h3>
            {searchResults.map((result, idx) => (
              <div
                key={`${result.apn}-${idx}`}
                className="flex justify-between items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <div>
                  <div className="font-medium text-gray-900">APN: {result.apn}</div>
                  {result.address && <div className="text-sm text-gray-600">{result.address}</div>}
                  <div className="text-sm text-gray-500">
                    {result.owner_name && <span>Owner: {result.owner_name}</span>}
                    {result.parcel_type && <span className="ml-3">Type: {result.parcel_type}</span>}
                  </div>
                </div>
                <button
                  onClick={() => handleAdd(result.apn)}
                  disabled={adding}
                  className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium disabled:opacity-50"
                >
                  {adding ? 'Adding...' : 'Add'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="bg-white rounded-xl shadow p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Add by APN Directly</h2>
        <form onSubmit={handleDirectAdd} className="flex gap-3">
          <input
            type="text"
            value={apn}
            onChange={(e) => setApn(e.target.value)}
            placeholder="Enter APN (e.g., 123-45-678)"
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          />
          <button
            type="submit"
            disabled={adding}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium disabled:opacity-50"
          >
            {adding ? 'Adding...' : 'Add Property'}
          </button>
        </form>
      </div>
    </div>
  );
}
