import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import InputOTPForm from "../components/InputOTPForm";
import { useAuth } from "../context/AuthContext";
import { jwtDecode } from "jwt-decode";

const PENDING_SIGNUP_KEY = "pendingSignup";

export default function OtpVerification() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const API_BASE = "http://localhost:8080";

  const getSignupData = () => {
    if (location.state?.name && location.state?.email && location.state?.password) {
      return location.state;
    }

    try {
      const raw = sessionStorage.getItem(PENDING_SIGNUP_KEY);
      if (!raw) return null;

      const parsed = JSON.parse(raw);
      if (!parsed?.name || !parsed?.email || !parsed?.password) {
        return null;
      }

      return parsed;
    } catch {
      return null;
    }
  };

  const signupData = getSignupData();

  useEffect(() => {
    if (location.state?.name && location.state?.email && location.state?.password) {
      sessionStorage.setItem(PENDING_SIGNUP_KEY, JSON.stringify(location.state));
    }
  }, [location.state]);

  const redirectByRole = (token) => {
    try {
      const claims = jwtDecode(token);
      const rawRole =
        claims?.role ||
        (Array.isArray(claims?.roles) && claims.roles[0]) ||
        (Array.isArray(claims?.authorities) && claims.authorities[0]) ||
        "USER";

      const normalized = String(rawRole).replace(/^ROLE_/i, "").toUpperCase();
      navigate(normalized === "ADMIN" ? "/admin" : "/dashboard", { replace: true });
    } catch {
      navigate("/dashboard", { replace: true });
    }
  };

  useEffect(() => {
    if (!signupData?.name || !signupData?.email || !signupData?.password) {
      navigate("/signup");
    }
  }, [signupData, navigate]);

  if (!signupData?.name || !signupData?.email || !signupData?.password) {
    return null;
  }

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

  const handleResend = async () => {
    const res = await fetch(`${API_BASE}/api/auth/send-otp`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email: signupData.email }),
    });

    if (!res.ok) {
      throw new Error(await getErrorMessage(res, "Unable to resend OTP"));
    }
  };

  const handleVerify = async (otp) => {
    const res = await fetch(`${API_BASE}/api/auth/verify-otp`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name: signupData.name,
        email: signupData.email,
        password: signupData.password,
        role: signupData.role || "USER",
        otp,
      }),
    });

    if (!res.ok) {
      throw new Error(await getErrorMessage(res, "OTP verification failed"));
    }

    const data = await res.json();
    if (!data.token) {
      throw new Error("Token missing in verification response");
    }

    const isLoggedIn = login(data.token);
    if (!isLoggedIn) {
      throw new Error("Invalid token returned from server");
    }

    sessionStorage.removeItem(PENDING_SIGNUP_KEY);
    redirectByRole(data.token);
  };

  return (
    <InputOTPForm
      email={signupData.email}
      initialOtp={signupData.devOtp || ""}
      initialInfo={signupData.infoMessage || ""}
      onVerify={handleVerify}
      onResend={handleResend}
    />
  );
}
