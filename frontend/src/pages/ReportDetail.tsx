import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getReport } from '../api/reports';
import type { Report } from '../types';

export default function ReportDetail() {
  const { id } = useParams<{ id: string }>();
  const [report, setReport] = useState<Report | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      getReport(id)
        .then(setReport)
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    );
  }

  if (!report) {
    return <div className="text-center py-12 text-gray-500">Report not found.</div>;
  }

  const data = (report.report_data || {}) as Record<string, unknown>;
  const summary = (data.property_summary || {}) as Record<string, unknown>;
  const ownership = (data.ownership || {}) as Record<string, unknown>;
  const valuations = data.valuations;

  return (
    <div className="max-w-4xl mx-auto">
      <Link to="/reports" className="text-sm text-blue-600 hover:text-blue-500 mb-4 inline-block">
        &larr; Back to Reports
      </Link>

      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Report Details</h1>
          <p className="mt-1 text-gray-600">
            Generated on {new Date(report.generated_at).toLocaleString()}
          </p>
        </div>
        <span
          className={`px-3 py-1 rounded-full text-sm font-medium ${
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

      <div className="space-y-6">
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Property Summary</h2>
          <dl className="grid grid-cols-2 gap-4">
            <div>
              <dt className="text-sm text-gray-500">APN</dt>
              <dd className="font-medium text-gray-900">{(summary.apn as string) || 'N/A'}</dd>
            </div>
            <div>
              <dt className="text-sm text-gray-500">Parcel Type</dt>
              <dd className="font-medium text-gray-900">{(summary.parcel_type as string) || 'N/A'}</dd>
            </div>
          </dl>
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Ownership Details</h2>
          {ownership.owner_details ? (
            <pre className="text-sm text-gray-700 bg-gray-50 p-4 rounded-lg overflow-auto max-h-64">
              {JSON.stringify(ownership.owner_details, null, 2)}
            </pre>
          ) : (
            <p className="text-gray-500">No ownership data available.</p>
          )}
        </div>

        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Valuation Data</h2>
          {valuations ? (
            <pre className="text-sm text-gray-700 bg-gray-50 p-4 rounded-lg overflow-auto max-h-64">
              {JSON.stringify(valuations, null, 2)}
            </pre>
          ) : (
            <p className="text-gray-500">No valuation data available.</p>
          )}
        </div>

        {report.email_sent_to && (
          <div className="bg-white rounded-xl shadow p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Email Details</h2>
            <dl className="grid grid-cols-2 gap-4">
              <div>
                <dt className="text-sm text-gray-500">Sent To</dt>
                <dd className="font-medium text-gray-900">{report.email_sent_to}</dd>
              </div>
              <div>
                <dt className="text-sm text-gray-500">Sent At</dt>
                <dd className="font-medium text-gray-900">
                  {report.sent_at ? new Date(report.sent_at).toLocaleString() : 'N/A'}
                </dd>
              </div>
            </dl>
          </div>
        )}
      </div>
    </div>
  );
}
