import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [oauthReady, setOauthReady] = useState({ google: false, github: false });
  const API_BASE = "http://localhost:8080";

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const oauthToken = params.get("token");
    const oauthError = params.get("error");

    if (oauthToken) {
      localStorage.setItem("token", oauthToken);
      navigate("/dashboard");
      return;
    }

    if (oauthError) {
      alert("OAuth login failed: provider did not return a usable email.");
      navigate("/login", { replace: true });
    }
  }, [navigate]);

  useEffect(() => {
    const loadOAuthConfig = async () => {
      try {
        const res = await fetch(`${API_BASE}/api/auth/oauth-config`);
        if (!res.ok) return;
        const data = await res.json();
        setOauthReady({
          google: !!data.google,
          github: !!data.github,
        });
      } catch {
        // Keep buttons disabled if backend is unreachable or misconfigured.
      }
    };

    loadOAuthConfig();
  }, []);

  const getErrorMessage = async (res, fallback) => {
    try {
      const contentType = res.headers.get("content-type") || "";
      if (contentType.includes("application/json")) {
        const data = await res.json();
        return data.message || data.error || fallback;
      }
      const text = await res.text();
      return text || fallback;
    } catch {
      return fallback;
    }
  };

  const handleOAuthLogin = (provider) => {
    if (!oauthReady[provider]) {
      alert(`${provider} OAuth is not configured on backend yet. Add client ID/secret first.`);
      return;
    }
    window.location.href = `${API_BASE}/oauth2/authorization/${provider}`;
  };

  const handleLogin = async () => {
    if (!email || !password) {
      alert("Enter credentials");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const errorMessage = await getErrorMessage(res, "Login failed");
        alert(errorMessage);
        return;
      }

      const data = await res.json();

      if (data.token) {
        localStorage.setItem("token", data.token);
        navigate("/dashboard");
      } else {
        alert("Login failed: token missing in response");
      }
    } catch (error) {
      alert("Login failed: cannot reach backend. Check Spring Boot server/CORS.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      
     <div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-lg hover:scale-[1.02] transition duration-300">
        
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-6">
          <div className="w-4 h-4 bg-blue-500 rounded"></div>
          <h1 className="text-lg font-semibold">IDVE</h1>
        </div>

        {/* Heading */}
        <h2 className="text-2xl font-semibold text-center mb-2">
          Log in to your account
        </h2>
        <p className="text-gray-500 text-center mb-6">
          Welcome back! Please enter your details.
        </p>

        {/* Email */}
        <div className="mb-4">
          <label className="text-sm text-gray-700">Email</label>
          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full mt-1 px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
        </div>

        {/* Password */}
        <div className="mb-4">
          <div className="flex justify-between text-sm text-gray-700">
            <label>Password</label>
            <span className="text-gray-500 cursor-pointer">Forgot?</span>
          </div>
          <input
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full mt-1 px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
        </div>

        {/* Sign in button */}
        <button
          onClick={handleLogin}
          className="w-full bg-black text-white py-2 rounded-lg mb-6 hover:opacity-90 transition duration-200"
        >
          Sign in
        </button>

        {/* Divider */}
        <div className="flex items-center gap-2 mb-6">
          <div className="flex-1 h-px bg-gray-300"></div>
          <span className="text-gray-400 text-sm">OR</span>
          <div className="flex-1 h-px bg-gray-300"></div>
        </div>

        {/* Google */}
        <button
          onClick={() => handleOAuthLogin("google")}
          disabled={!oauthReady.google}
          className="w-full border py-2 rounded-lg mb-3 hover:bg-gray-50 transition"
        >
          {oauthReady.google ? "Continue with Google" : "Google OAuth not configured"}
        </button>

        {/* GitHub */}
        <button
          onClick={() => handleOAuthLogin("github")}
          disabled={!oauthReady.github}
          className="w-full border py-2 rounded-lg hover:bg-gray-50 transition"
        >
          {oauthReady.github ? "Continue with GitHub" : "GitHub OAuth not configured"}
        </button>

        {/* Signup */}
        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{" "}
          <Link to="/signup" className="text-black font-medium cursor-pointer">
            Sign up
          </Link>
        </p>

      </div>
    </div>
  );
}