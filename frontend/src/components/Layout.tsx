import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-slate-800 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-8">
              <Link to="/dashboard" className="text-xl font-bold text-white hover:text-blue-200">
                Maricopa Property Reports
              </Link>
              {user && (
                <div className="hidden md:flex space-x-4">
                  <Link to="/dashboard" className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-700">
                    Dashboard
                  </Link>
                  <Link to="/properties" className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-700">
                    Properties
                  </Link>
                  <Link to="/reports" className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-700">
                    Reports
                  </Link>
                  <Link to="/schedules" className="px-3 py-2 rounded-md text-sm font-medium hover:bg-slate-700">
                    Schedules
                  </Link>
                </div>
              )}
            </div>
            {user && (
              <div className="flex items-center space-x-4">
                <span className="text-sm text-gray-300">{user.email}</span>
                <button
                  onClick={handleLogout}
                  className="px-3 py-2 rounded-md text-sm font-medium bg-red-600 hover:bg-red-700"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
}
