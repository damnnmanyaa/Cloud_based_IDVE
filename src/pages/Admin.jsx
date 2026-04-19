import { Link } from "react-router-dom";

export default function Admin() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-xl bg-white p-8 rounded-2xl shadow-lg text-center">
        <h1 className="text-3xl font-semibold mb-3">Admin Panel</h1>
        <p className="text-gray-600 mb-6">
          You are signed in with ADMIN access.
        </p>
        <Link
          to="/dashboard"
          className="inline-block bg-black text-white px-5 py-2 rounded-lg hover:opacity-90 transition"
        >
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
