import { useEffect, useState } from 'react';
import { listSchedules, createSchedule, updateSchedule, deleteSchedule } from '../api/schedules';
import { listProperties } from '../api/properties';
import type { Schedule, Property } from '../types';

export default function Schedules() {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [selectedProperty, setSelectedProperty] = useState('');
  const [frequency, setFrequency] = useState('weekly');
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState('');

  const fetchData = () => {
    setLoading(true);
    Promise.all([listSchedules(), listProperties()])
      .then(([scheds, props]) => {
        setSchedules(scheds);
        setProperties(props);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async () => {
    if (!selectedProperty) return;
    setCreating(true);
    setError('');
    try {
      await createSchedule(selectedProperty, frequency);
      setShowForm(false);
      setSelectedProperty('');
      fetchData();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { detail?: string } } };
        setError(axiosErr.response?.data?.detail || 'Failed to create schedule');
      } else {
        setError('Failed to create schedule');
      }
    } finally {
      setCreating(false);
    }
  };

  const handleToggle = async (schedule: Schedule) => {
    await updateSchedule(schedule.id, { is_active: !schedule.is_active });
    fetchData();
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this schedule?')) return;
    await deleteSchedule(id);
    fetchData();
  };

  const getPropertyApn = (propertyId: string) => {
    const prop = properties.find((p) => p.id === propertyId);
    return prop?.apn || propertyId;
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
        <h1 className="text-3xl font-bold text-gray-900">Report Schedules</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium"
        >
          {showForm ? 'Cancel' : 'New Schedule'}
        </button>
      </div>

      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {showForm && (
        <div className="bg-white rounded-xl shadow p-6 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Create New Schedule</h2>
          {properties.length === 0 ? (
            <p className="text-gray-500">Add a property first to create a schedule.</p>
          ) : (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Property</label>
                <select
                  value={selectedProperty}
                  onChange={(e) => setSelectedProperty(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">Select a property...</option>
                  {properties.map((prop) => (
                    <option key={prop.id} value={prop.id}>
                      {prop.apn} - {prop.property_address || 'No address'}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Frequency</label>
                <select
                  value={frequency}
                  onChange={(e) => setFrequency(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="daily">Daily</option>
                  <option value="weekly">Weekly</option>
                  <option value="monthly">Monthly</option>
                  <option value="quarterly">Quarterly</option>
                </select>
              </div>
              <button
                onClick={handleCreate}
                disabled={creating || !selectedProperty}
                className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium disabled:opacity-50"
              >
                {creating ? 'Creating...' : 'Create Schedule'}
              </button>
            </div>
          )}
        </div>
      )}

      {schedules.length === 0 ? (
        <div className="bg-white rounded-xl shadow p-12 text-center">
          <p className="text-gray-500 text-lg">No schedules set up yet.</p>
          <p className="mt-2 text-gray-400">
            Create a schedule to receive periodic property reports via email.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Property
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Frequency
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Next Run
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {schedules.map((schedule) => (
                <tr key={schedule.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {getPropertyApn(schedule.property_id)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 capitalize">
                    {schedule.frequency}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(schedule.next_run_at).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 py-1 text-xs rounded-full ${
                        schedule.is_active
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      {schedule.is_active ? 'Active' : 'Paused'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm space-x-2">
                    <button
                      onClick={() => handleToggle(schedule)}
                      className="text-blue-600 hover:text-blue-500 font-medium"
                    >
                      {schedule.is_active ? 'Pause' : 'Resume'}
                    </button>
                    <button
                      onClick={() => handleDelete(schedule.id)}
                      className="text-red-600 hover:text-red-500 font-medium"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
