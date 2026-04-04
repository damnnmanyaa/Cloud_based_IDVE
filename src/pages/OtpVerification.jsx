import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import InputOTPForm from "../components/InputOTPForm";

export default function OtpVerification() {
  const navigate = useNavigate();
  const location = useLocation();
  const API_BASE = "http://localhost:8080";

  const signupData = location.state;

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

    localStorage.setItem("token", data.token);
    navigate("/dashboard");
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
