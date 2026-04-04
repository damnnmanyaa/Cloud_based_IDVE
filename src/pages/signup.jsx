import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

export default function Signup() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [oauthReady, setOauthReady] = useState({ google: false, github: false });
  const API_BASE = "http://localhost:8080";

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
        // Keep OAuth disabled when backend is unreachable.
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

  const handleSignup = async () => {
    if (!name || !email || !password) {
      alert("Enter all signup details");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/auth/send-otp`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email }),
      });

      if (res.ok) {
        const data = await res.json();
        navigate("/verify-otp", {
          state: {
            name,
            email,
            password,
            devOtp: data?.otp || "",
            infoMessage: data?.message || "",
          },
        });
      } else {
        const errorMessage = await getErrorMessage(res, "Signup failed");
        alert(errorMessage);
      }
    } catch (error) {
      alert("Signup failed: cannot reach backend. Check Spring Boot server/CORS.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
      
      <div className="w-full max-w-md text-center">
        
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-6">
          <div className="w-5 h-5 bg-black rounded"></div>
          <h1 className="text-lg font-semibold">IDVE</h1>
        </div>

        {/* Heading */}
        <h2 className="text-4xl font-bold mb-8">
          Sign up for an account
        </h2>

        {/* Social Buttons */}
        <div className="flex gap-4 mb-6">
          
          <button
            onClick={() => handleOAuthLogin("github")}
            disabled={!oauthReady.github}
            className="flex-1 border rounded-lg py-3 hover:bg-gray-100 transition"
          >
            {oauthReady.github ? "Login with GitHub" : "GitHub not configured"}
          </button>

          <button
            onClick={() => handleOAuthLogin("google")}
            disabled={!oauthReady.google}
            className="flex-1 border rounded-lg py-3 hover:bg-gray-100 transition"
          >
            {oauthReady.google ? "Login with Google" : "Google not configured"}
          </button>

        </div>

        {/* Divider */}
        <div className="flex items-center gap-2 mb-6">
          <div className="flex-1 h-px bg-gray-300"></div>
          <span className="text-gray-400 text-sm"></span>
          <div className="flex-1 h-px bg-gray-300"></div>
        </div>

        {/* Signup fields */}
        <div className="space-y-3 mb-6 text-left">
          <input
            type="text"
            placeholder="Full name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
          <input
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
        </div>

        {/* Email Button */}
        <button
          onClick={handleSignup}
          className="w-full bg-black text-white py-3 rounded-lg hover:opacity-90 transition duration-200"
        >
          Continue with Email
        </button>
          
          {/* login */}
        <p className="text-center text-sm text-gray-500 mt-6">
          Already have an account?{" "}
          <Link to="/login" className="text-black font-medium cursor-pointer">
            Log in
          </Link>
        </p>
        

      </div>
    </div>
  );
}

 