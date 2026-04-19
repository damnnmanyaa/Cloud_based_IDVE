import { Navigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { useAuth } from "../context/AuthContext";

const normalizeRole = (value) =>
  String(value || "")
    .replace(/^ROLE_/i, "")
    .toUpperCase();

const decodeRoleFromToken = (token) => {
  if (!token) return "";

  try {
    const claims = jwtDecode(token);
    return normalizeRole(
      claims?.role ||
        (Array.isArray(claims?.roles) ? claims.roles[0] : "") ||
        (Array.isArray(claims?.authorities) ? claims.authorities[0] : "")
    );
  } catch {
    return "";
  }
};

export default function RoleRoute({ role, children }) {
  const { role: currentRole } = useAuth();
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  const expectedRole = normalizeRole(role);
  const effectiveRole = normalizeRole(currentRole) || decodeRoleFromToken(token);

  if (effectiveRole !== expectedRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
