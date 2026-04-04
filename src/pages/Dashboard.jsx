import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Dashboard() {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  useEffect(() => {
    const fetchCurrentUser = async () => {
      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      try {
        const res = await fetch("http://localhost:8080/api/user/me", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!res.ok) {
          localStorage.removeItem("token");
          navigate("/login");
          return;
        }

        const user = await res.json();
        setCurrentUser(user);
      } catch (error) {
        localStorage.removeItem("token");
        navigate("/login");
      }
    };

    fetchCurrentUser();
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Navbar */}
      <div className="flex justify-between items-center px-8 py-4 bg-white border-b">
        
        {/* Logo */}
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 bg-blue-500 rounded-md"></div>
          <h1 className="text-lg font-semibold">IDVE</h1>
        </div>

        {/* Right side */}
        <div className="flex items-center gap-4">
          {currentUser && (
            <div className="text-right leading-tight">
              <p className="text-sm font-medium text-gray-800">{currentUser.name}</p>
              <p className="text-xs text-gray-500">{currentUser.email}</p>
            </div>
          )}

          <button className="px-4 py-2 text-sm rounded-lg border hover:bg-gray-100 transition">
            Dashboard
          </button>

          <Link
            to="/upload"
            className="px-4 py-2 text-sm bg-blue-500 text-white rounded-lg hover:opacity-90 transition"
          >
            Upload
          </Link>

          <button
            onClick={handleLogout}
            className="px-4 py-2 text-sm bg-black text-white rounded-lg hover:opacity-90 transition"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Hero Section */}
      <div className="flex flex-col items-center justify-center text-center mt-24 px-6">

        {/* Tag */}
        <div className="mb-6 px-4 py-1 text-sm bg-blue-100 text-blue-600 rounded-full">
          Identity Verification System
        </div>

        {/* Heading */}
        <h2 className="text-4xl md:text-5xl font-bold mb-6">
          Verify your identity <br /> securely and easily
        </h2>

        {/* Subtext */}
        <p className="text-gray-500 max-w-xl mb-8">
          Upload your documents and track verification status in real-time.
          Our system ensures secure and fast identity validation.
        </p>

        {/* CTA */}
        <Link
          to="/upload"
          className="bg-blue-500 text-white px-6 py-3 rounded-lg hover:opacity-90 transition"
        >
          Get Started
        </Link>

        {/* Small note */}
        <p className="text-gray-400 text-sm mt-4">
          Secure • Fast • Reliable
        </p>
      </div>

      {/* Bottom Section (optional preview card) */}
      <div className="mt-16 px-6 flex justify-center">
        <div className="w-full max-w-4xl bg-white border rounded-xl shadow-sm p-6">
          <h3 className="text-lg font-medium mb-2">Verification Status</h3>
          <p className="text-yellow-500 font-semibold">Pending </p>
        </div>
      </div>

    </div>
  );
}