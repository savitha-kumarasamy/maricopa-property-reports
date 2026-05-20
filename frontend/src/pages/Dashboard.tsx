import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { listProperties } from '../api/properties';
import { listReports } from '../api/reports';
import { listSchedules } from '../api/schedules';
import type { Property, Report, Schedule } from '../types';

export default function Dashboard() {
  const { user } = useAuth();
  const [properties, setProperties] = useState<Property[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([listProperties(), listReports(), listSchedules()])
      .then(([props, reps, scheds]) => {
        setProperties(props);
        setReports(reps.reports.slice(0, 5));
        setSchedules(scheds);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Welcome, {user?.full_name}
        </h1>
        <p className="mt-1 text-gray-600">Here's an overview of your property tracking.</p>
      </div>

      <div className="grid md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow p-6">
          <div className="text-sm font-medium text-gray-500">Properties Tracked</div>
          <div className="mt-2 text-3xl font-bold text-gray-900">{properties.length}</div>
          <Link to="/properties" className="mt-2 inline-block text-sm text-blue-600 hover:text-blue-500">
            View all &rarr;
          </Link>
        </div>
        <div className="bg-white rounded-xl shadow p-6">
          <div className="text-sm font-medium text-gray-500">Reports Generated</div>
          <div className="mt-2 text-3xl font-bold text-gray-900">{reports.length}</div>
          <Link to="/reports" className="mt-2 inline-block text-sm text-blue-600 hover:text-blue-500">
            View all &rarr;
          </Link>
        </div>
        <div className="bg-white rounded-xl shadow p-6">
          <div className="text-sm font-medium text-gray-500">Active Schedules</div>
          <div className="mt-2 text-3xl font-bold text-gray-900">
            {schedules.filter((s) => s.is_active).length}
          </div>
          <Link to="/schedules" className="mt-2 inline-block text-sm text-blue-600 hover:text-blue-500">
            Manage &rarr;
          </Link>
        </div>
      </div>

      <div className="grid md:grid-cols-2 gap-8">
        <div className="bg-white rounded-xl shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Your Properties</h2>
          </div>
          <div className="p-6">
            {properties.length === 0 ? (
              <div className="text-center py-4">
                <p className="text-gray-500">No properties tracked yet.</p>
                <Link
                  to="/properties/add"
                  className="mt-2 inline-block text-blue-600 hover:text-blue-500 font-medium"
                >
                  Add your first property &rarr;
                </Link>
              </div>
            ) : (
              <div className="space-y-3">
                {properties.slice(0, 5).map((prop) => (
                  <Link
                    key={prop.id}
                    to={`/properties/${prop.id}`}
                    className="block p-3 rounded-lg hover:bg-gray-50 border border-gray-100"
                  >
                    <div className="font-medium text-gray-900">{prop.apn}</div>
                    <div className="text-sm text-gray-500">{prop.property_address || 'Address not available'}</div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="bg-white rounded-xl shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Recent Reports</h2>
          </div>
          <div className="p-6">
            {reports.length === 0 ? (
              <p className="text-center py-4 text-gray-500">No reports generated yet.</p>
            ) : (
              <div className="space-y-3">
                {reports.map((report) => (
                  <Link
                    key={report.id}
                    to={`/reports/${report.id}`}
                    className="block p-3 rounded-lg hover:bg-gray-50 border border-gray-100"
                  >
                    <div className="flex justify-between items-center">
                      <div className="font-medium text-gray-900">{report.report_type} Report</div>
                      <span
                        className={`px-2 py-1 text-xs rounded-full ${
                          report.status === 'sent'
                            ? 'bg-green-100 text-green-800'
                            : report.status === 'generated'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {report.status}
                      </span>
                    </div>
                    <div className="text-sm text-gray-500">
                      {new Date(report.generated_at).toLocaleDateString()}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
