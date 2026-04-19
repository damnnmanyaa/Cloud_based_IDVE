import { useEffect, useMemo, useRef, useState } from "react";

export default function InputOTPForm({ email, onVerify, onResend, initialOtp = "", initialInfo = "" }) {
  const [otp, setOtp] = useState(initialOtp || "");
  const [error, setError] = useState("");
  const [info, setInfo] = useState(initialInfo || "");
  const [isVerifying, setIsVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const otpRefs = useRef([]);

  useEffect(() => {
    if (initialOtp) {
      setOtp(initialOtp.slice(0, 6));
      setInfo(initialInfo || "Dev mode: OTP pre-filled from backend response");
    }
  }, [initialOtp, initialInfo]);

  const maskedEmail = useMemo(() => {
    if (!email || !email.includes("@")) return "m@example.com";
    const [name, domain] = email.split("@");
    const first = name.slice(0, 1) || "m";
    return `${first}@${domain}`;
  }, [email]);

  const handleOtpChange = (value) => {
    const digits = value.replace(/\D/g, "").slice(0, 6);
    setOtp(digits);
    setError("");
  };

  const setOtpCharAt = (index, char) => {
    const chars = otp.padEnd(6, " ").split("");
    chars[index] = char || " ";
    setOtp(chars.join("").replace(/\s/g, ""));
    setError("");
  };

  const handleBoxChange = (index, value) => {
    const digit = value.replace(/\D/g, "").slice(-1);
    setOtpCharAt(index, digit);

    if (digit && index < 5) {
      otpRefs.current[index + 1]?.focus();
      otpRefs.current[index + 1]?.select();
    }
  };

  const handleBoxKeyDown = (index, e) => {
    if (e.key !== "Backspace") {
      return;
    }

    const current = otp[index] || "";
    if (current) {
      setOtpCharAt(index, "");
      return;
    }

    if (index > 0) {
      setOtpCharAt(index - 1, "");
      otpRefs.current[index - 1]?.focus();
    }
  };

  const handleBoxPaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (!pasted) return;

    handleOtpChange(pasted);

    const nextIndex = Math.min(pasted.length, 5);
    otpRefs.current[nextIndex]?.focus();
  };

  const handleVerify = async () => {
    if (otp.length !== 6) {
      setError("Please enter 6-digit OTP");
      return;
    }

    setIsVerifying(true);
    setError("");
    setInfo("");
    try {
      await onVerify(otp);
    } catch (e) {
      setError(e?.message || "OTP verification failed");
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    setIsResending(true);
    setError("");
    setInfo("");
    setOtp("");
    try {
      await onResend();
      setInfo("OTP sent successfully");
    } catch (e) {
      setError(e?.message || "Unable to resend OTP");
    } finally {
      setIsResending(false);
    }
  };

  const renderOtpInput = (index) => (
    <input
      key={index}
      ref={(el) => {
        otpRefs.current[index] = el;
      }}
      type="text"
      inputMode="numeric"
      maxLength={1}
      value={otp[index] || ""}
      onChange={(e) => handleBoxChange(index, e.target.value)}
      onKeyDown={(e) => handleBoxKeyDown(index, e)}
      onPaste={handleBoxPaste}
      className="w-14 h-14 border border-gray-300 rounded-md text-center text-3xl bg-white outline-none focus:ring-2 focus:ring-gray-400"
      aria-label={`OTP digit ${index + 1}`}
    />
  );

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div className="w-full max-w-[520px] bg-white border border-gray-200 rounded-3xl shadow-sm">
        <div className="p-6">
          <h2 className="text-4xl font-semibold mb-2">Verify your login</h2>
          <p className="text-gray-600 text-2xl leading-relaxed mb-6">
            Enter the verification code we sent to your
            <br />
            email address: <span className="font-semibold">{maskedEmail}</span>.
          </p>

          <div className="flex items-center justify-between mb-2">
            <p className="text-3xl font-semibold">Verification code</p>
            <button
              onClick={handleResend}
              disabled={isResending}
              className="px-4 py-2 border border-gray-300 rounded-xl text-2xl font-semibold transition hover:bg-gray-100 disabled:opacity-60"
            >
              {isResending ? "Resending..." : "Resend Code"}
            </button>
          </div>

          <div className="mt-3 mb-4">
            <div className="flex items-center gap-4">
              <div className="flex gap-0.5">
                {renderOtpInput(0)}
                {renderOtpInput(1)}
                {renderOtpInput(2)}
              </div>
              <span className="text-4xl font-medium">-</span>
              <div className="flex gap-0.5">
                {renderOtpInput(3)}
                {renderOtpInput(4)}
                {renderOtpInput(5)}
              </div>
            </div>
          </div>

        </div>

        <div className="border-t border-gray-200 p-6">
          <button
            onClick={handleVerify}
            disabled={isVerifying}
            className="w-full h-14 bg-black text-white text-3xl rounded-2xl font-semibold transition hover:opacity-90 disabled:opacity-60"
          >
            {isVerifying ? "Verifying..." : "Verify"}
          </button>

          <p className="text-center text-gray-600 text-2xl mt-4">
            Having trouble signing in? <span className="underline">Contact support</span>
          </p>

          {error && <p className="text-red-600 text-xl mt-4">{error}</p>}
          {info && <p className="text-green-600 text-xl mt-4">{info}</p>}
        </div>
      </div>
    </div>
  );
}
