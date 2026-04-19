import axios from "axios";

const API_BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "token";

const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

let isUnauthorizedHandlingInProgress = false;

const handleUnauthorized = () => {
  if (isUnauthorizedHandlingInProgress) return;

  isUnauthorizedHandlingInProgress = true;
  localStorage.removeItem(TOKEN_KEY);
  window.location.replace("/login");
};

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      handleUnauthorized();
    }

    return Promise.reject(error);
  }
);

export { api, API_BASE_URL };
export default api;
