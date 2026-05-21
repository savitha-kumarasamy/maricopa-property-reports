import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Landing() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-800 to-blue-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-16">
        <div className="text-center">
          <h1 className="text-5xl font-extrabold tracking-tight sm:text-6xl">
            Maricopa County
            <span className="block text-blue-300">Property Reports</span>
          </h1>
          <p className="mt-6 max-w-2xl mx-auto text-xl text-gray-300">
            Track your Maricopa County property ownership details, valuations, and tax information.
            Get periodic reports delivered straight to your inbox.
          </p>
          <div className="mt-10 flex justify-center gap-4">
            {user ? (
              <Link
                to="/dashboard"
                className="px-8 py-3 bg-blue-600 hover:bg-blue-700 rounded-lg text-lg font-semibold transition"
              >
                Go to Dashboard
              </Link>
            ) : (
              <>
                <Link
                  to="/register"
                  className="px-8 py-3 bg-blue-600 hover:bg-blue-700 rounded-lg text-lg font-semibold transition"
                >
                  Get Started
                </Link>
                <Link
                  to="/login"
                  className="px-8 py-3 bg-white/10 hover:bg-white/20 rounded-lg text-lg font-semibold transition border border-white/20"
                >
                  Sign In
                </Link>
              </>
            )}
          </div>
        </div>

        <div className="mt-24 grid md:grid-cols-3 gap-8">
          <div className="bg-white/10 rounded-xl p-6 backdrop-blur-sm">
            <div className="text-3xl mb-4">🏠</div>
            <h3 className="text-xl font-bold mb-2">Track Properties</h3>
            <p className="text-gray-300">
              Add your Maricopa County properties by APN or search by address. View detailed ownership and valuation data.
            </p>
          </div>
          <div className="bg-white/10 rounded-xl p-6 backdrop-blur-sm">
            <div className="text-3xl mb-4">📊</div>
            <h3 className="text-xl font-bold mb-2">Generate Reports</h3>
            <p className="text-gray-300">
              Get comprehensive reports on property ownership, tax assessments, and valuation history at any time.
            </p>
          </div>
          <div className="bg-white/10 rounded-xl p-6 backdrop-blur-sm">
            <div className="text-3xl mb-4">📧</div>
            <h3 className="text-xl font-bold mb-2">Periodic Updates</h3>
            <p className="text-gray-300">
              Set up daily, weekly, monthly, or quarterly email reports to stay informed about your properties.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
