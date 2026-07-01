import axios from 'axios';
import { toast } from 'react-hot-toast';

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
});

instance.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            const status = error.response.status;
            
            if (status === 401) {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("user");
                toast.error("Session expired or unauthorized. Please log in again.");
                if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
                    window.location.href = '/login';
                }
            } else if (status === 403) {
                toast.error(error.response.data?.message || "You don't have permission to perform this action.");
            } else if (status >= 500) {
                toast.error("A server error occurred. Please try again later.");
            }
        } else if (error.request) {
            toast.error("Network error. Please check your connection.");
        }
        return Promise.reject(error);
    }
);

export default instance;
