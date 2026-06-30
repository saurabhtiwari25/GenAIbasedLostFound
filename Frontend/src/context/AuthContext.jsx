import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const savedUserId = localStorage.getItem("userId");
        const savedUserName = localStorage.getItem("userName");
        if (savedUserId && savedUserName) {
            setUser({ id: savedUserId, name: savedUserName });
        }
    }, []);

    const login = (id, name) => {
        localStorage.setItem("userId", id);
        localStorage.setItem("userName", name);
        setUser({ id, name });
    };

    const logout = () => {
        localStorage.removeItem("userId");
        localStorage.removeItem("userName");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};