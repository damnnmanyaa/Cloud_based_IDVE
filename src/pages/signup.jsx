import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

const PENDING_SIGNUP_KEY = "pendingSignup";

export default function Signup() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [role, setRole] = useState("USER");
  const [isSendingOtp, setIsSendingOtp] = useState(false);
  const [oauthReady, setOauthReady] = useState({ google: false, github: false });
  const API_BASE = "http://localhost:8080";

  useEffect(() => {
    const clearForm = () => {
      setName("");
      setEmail("");
      setPassword("");
      setRole("USER");
      setShowPassword(false);
    };

    clearForm();
    const timer = setTimeout(clearForm, 120);
    window.addEventListener("pageshow", clearForm);

    sessionStorage.removeItem(PENDING_SIGNUP_KEY);

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

    return () => {
      clearTimeout(timer);
      window.removeEventListener("pageshow", clearForm);
    };
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
    if (isSendingOtp) {
      return;
    }

    if (!name || !email || !password) {
      alert("Enter all signup details");
      return;
    }

    setIsSendingOtp(true);

    try {
      const res = await fetch(`${API_BASE}/api/auth/send-otp`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, role }),
      });

      if (res.ok) {
        const data = await res.json();
        const pendingSignup = {
          name,
          email,
          password,
          role,
          devOtp: data?.otp || "",
          infoMessage: data?.message || "",
        };

        sessionStorage.setItem(PENDING_SIGNUP_KEY, JSON.stringify(pendingSignup));

        navigate("/verify-otp", {
          state: {
            ...pendingSignup,
          },
        });
      } else {
        const errorMessage = await getErrorMessage(res, "Signup failed");
        alert(errorMessage);
      }
    } catch (error) {
      alert("Signup failed: cannot reach backend. Check Spring Boot server/CORS.");
    } finally {
      setIsSendingOtp(false);
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
            name="signup-full-name"
            autoComplete="off"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
          <input
            type="email"
            placeholder="you@example.com"
            name="signup-email-address"
            autoComplete="off"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
          />
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="••••••••"
              name="signup-password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 pr-14 border rounded-lg outline-none focus:ring-2 focus:ring-gray-300"
            />
            <button
              type="button"
              onClick={() => setShowPassword((prev) => !prev)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-600 hover:text-gray-900"
            >
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setRole("USER")}
                className={`rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
                  role === "USER"
                    ? "bg-black text-white"
                    : "bg-gray-100 text-gray-800 border border-gray-300 hover:bg-gray-200"
                }`}
              >
                as User
              </button>
              <button
                type="button"
                onClick={() => setRole("ADMIN")}
                className={`rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200 ${
                  role === "ADMIN"
                    ? "bg-black text-white"
                    : "bg-gray-100 text-gray-800 border border-gray-300 hover:bg-gray-200"
                }`}
              >
                as Admin
              </button>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              Selected: {role === "ADMIN" ? "Admin" : "User"}
            </p>
          </div>
        </div>

        {/* Email Button */}
        <button
          onClick={handleSignup}
          disabled={isSendingOtp}
          className="w-full bg-black text-white py-3 rounded-lg hover:opacity-90 transition duration-200 disabled:opacity-70"
        >
          {isSendingOtp ? "Sending OTP..." : "Continue with Email"}
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

 