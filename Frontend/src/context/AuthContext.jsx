import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const savedUserName = localStorage.getItem("userName");
        const token = localStorage.getItem("accessToken");
        if (savedUserName && token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                setUser({ id: payload.userId, name: savedUserName });
            } catch (e) {
                console.error("Invalid token", e);
                setUser(null);
            }
        }
    }, []);

    const login = (id, name, token) => {
        localStorage.setItem("userName", name);
        if (token) {
            localStorage.setItem("accessToken", token);
        }
        setUser({ id, name });
    };

    const logout = () => {
        localStorage.removeItem("userName");
        localStorage.removeItem("accessToken");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};