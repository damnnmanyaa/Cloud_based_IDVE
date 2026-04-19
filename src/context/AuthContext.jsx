import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { jwtDecode } from "jwt-decode";

const TOKEN_KEY = "token";

const AuthContext = createContext(undefined);

const getRoleFromClaims = (claims) => {
  if (!claims || typeof claims !== "object") return null;

  if (typeof claims.role === "string") return claims.role;
  if (Array.isArray(claims.roles) && claims.roles.length > 0) return claims.roles[0];
  if (Array.isArray(claims.authorities) && claims.authorities.length > 0) {
    return claims.authorities[0];
  }

  return null;
};

const getUserFromClaims = (claims) => {
  if (!claims || typeof claims !== "object") return null;

  return {
    id: claims.sub || claims.id || claims.userId || null,
    name: claims.name || claims.preferred_username || claims.username || null,
    email: claims.email || null,
  };
};

const decodeToken = (rawToken) => {
  if (!rawToken || typeof rawToken !== "string") return null;

  try {
    const claims = jwtDecode(rawToken);

    if (claims?.exp && Date.now() >= claims.exp * 1000) {
      return null;
    }

    return {
      user: getUserFromClaims(claims),
      role: getRoleFromClaims(claims),
    };
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const applyToken = useCallback((rawToken) => {
    const decoded = decodeToken(rawToken);

    if (!decoded) {
      setToken(null);
      setUser(null);
      setRole(null);
      setIsAuthenticated(false);
      return false;
    }

    setToken(rawToken);
    setUser(decoded.user);
    setRole(decoded.role);
    setIsAuthenticated(true);

    return true;
  }, []);

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (!storedToken) return;

    const isValid = applyToken(storedToken);
    if (!isValid) {
      localStorage.removeItem(TOKEN_KEY);
    }
  }, [applyToken]);

  const login = useCallback(
    (rawToken) => {
      const isValid = applyToken(rawToken);
      if (!isValid) return false;

      localStorage.setItem(TOKEN_KEY, rawToken);
      return true;
    },
    [applyToken]
  );

  const logout = useCallback(() => {
    localStorage.clear();
    setToken(null);
    setUser(null);
    setRole(null);
    setIsAuthenticated(false);
    window.location.replace("/login");
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      role,
      isAuthenticated,
      login,
      logout,
    }),
    [user, token, role, isAuthenticated, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
