import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import axios from '../api/axiosConfig';

const Footer = () => {
    const { user } = useContext(AuthContext);
    const year = new Date().getFullYear();
    const [isOnline, setIsOnline] = useState(true);

    useEffect(() => {
        if (!user) return;

        let isMounted = true;
        
        const checkStatus = async () => {
            try {
                await axios.get('/health'); 
                if (isMounted) setIsOnline(true);
            } catch (error) {
                if (isMounted) {
                    if (error.response) {
                        setIsOnline(true);
                    } else {
                        setIsOnline(false);
                    }
                }
            }
        };

        checkStatus();
        const interval = setInterval(checkStatus, 300000);

        return () => {
            isMounted = false;
            clearInterval(interval);
        };
    }, [user]);

    return (
        <footer className="app-footer">
            <div className="footer-content">
                <p>&copy; {year} Lost & Found System</p>
                
                <div className="status-indicator">
                    <span>System: 
                        {isOnline ? (
                            <strong style={{ color: '#28a745' }}> ● Online</strong>
                        ) : (
                            <strong style={{ color: '#dc3545' }}> ● Offline</strong>
                        )}
                    </span>
                    <span className="divider">|</span>
                    <span> 
                        User Session: <strong>{user ? (user.name || 'User') : 'Guest'}</strong>
                    </span>
                </div>
            </div>
        </footer>
    );
};

export default Footer;