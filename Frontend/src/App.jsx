import React, { useState, useEffect, useContext } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthContext } from './context/AuthContext';
import Navbar from './components/Navbar';
import ItemList from './components/ItemList';
import Login from './components/Login';
import Register from './components/Register';
import AddItem from './components/AddItem';
import ItemDetail from './components/ItemDetail';
import SearchItem from './components/SearchItem';
import Footer from './components/Footer';
import Dashboard from './components/Dashboard';
import { Toaster } from 'react-hot-toast';


const ProtectedRoute = ({ children }) => {
    const { user } = useContext(AuthContext);
    if (!user) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

const App = () => {
    const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme(prev => prev === 'light' ? 'dark' : 'light');
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <Toaster position="top-right" />
            <Navbar theme={theme} toggleTheme={toggleTheme} />
            <main className="container" style={{ flex: '1 0 auto' }}>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/" element={<ProtectedRoute><ItemList /></ProtectedRoute>} />
                    <Route path="/search" element={<ProtectedRoute><SearchItem /></ProtectedRoute>} />
                    <Route path="/item/:id" element={<ProtectedRoute><ItemDetail /></ProtectedRoute>} />
                    <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
                    <Route path="/add" element={<ProtectedRoute><AddItem /></ProtectedRoute>} />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </main>
            <Footer />
        </div>
    );
};

export default App;