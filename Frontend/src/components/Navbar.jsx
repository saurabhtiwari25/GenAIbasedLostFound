import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

const ThemeToggle = ({ theme, toggleTheme }) => {
    const isDark = theme === 'dark';
    return (
        <button
            onClick={toggleTheme}
            style={{
                width: '60px',
                height: '30px',
                borderRadius: '30px',
                border: '2px solid #6366f1',
                background: isDark ? '#1a1a1a' : '#f8fafc',
                position: 'relative',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                padding: '0',
                transition: 'background-color 0.3s ease',
                outline: 'none',
                marginLeft: '15px'
            }}
            aria-label="Toggle theme"
        >
            <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: '#6366f1',
                position: 'absolute',
                left: isDark ? 'calc(100% - 36px + 4px)' : '-4px',
                top: '-5px',
                transition: 'left 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 2px 8px rgba(99, 102, 241, 0.4)'
            }}>
                {isDark ? (
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0f172a" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                    </svg>
                ) : (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#facc15" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" fill="#facc15" opacity="0" />
                        <circle cx="12" cy="12" r="3.5" fill="none"></circle>
                        <path d="M12 2v2"></path>
                        <path d="M12 20v2"></path>
                        <path d="M4.93 4.93l1.41 1.41"></path>
                        <path d="M17.66 17.66l1.41 1.41"></path>
                        <path d="M2 12h2"></path>
                        <path d="M20 12h2"></path>
                        <path d="M6.34 17.66l-1.41 1.41"></path>
                        <path d="M19.07 4.93l-1.41 1.41"></path>
                    </svg>
                )}
            </div>
        </button>
    );
};

const Navbar = ({ theme, toggleTheme }) => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const location = useLocation();
    const currentView = location.pathname;

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="navbar">
            <div onClick={() => navigate('/')} style={{ cursor: 'pointer', display: 'flex', flexDirection: 'column', paddingTop: '4px' }}>
                <h2 style={{ margin: 0, lineHeight: '1.2', cursor: 'pointer' }}>
                    <span style={{ fontSize: '20px', fontWeight: '800', letterSpacing: '-0.5px' }}>Lost & Found System</span>
                </h2>
                <span style={{ fontSize: '11px', color: '#94a3b8', fontWeight: '700', letterSpacing: '0.5px', marginTop: '0px' }}>
                    Powered by A.I.
                </span>
            </div>

            <div className="nav-links">
                {user && (
                    <>
                        <button
                            onClick={() => navigate('/')}
                            className={currentView === '/' || currentView.startsWith('/item/') ? 'active' : ''}
                        >
                            View Items
                        </button>
                        <button
                            onClick={() => navigate('/search')}
                            className={currentView === '/search' ? 'active' : ''}
                        >
                            Search Items
                        </button>
                        <button
                            onClick={() => navigate('/add')}
                            className={currentView === '/add' ? 'active' : ''}
                        >
                            Report New Item
                        </button>
                    </>
                )}

                {user ? (
                    <>
                        <button
                            onClick={() => navigate('/dashboard')}
                            className={currentView === '/dashboard' ? 'active' : ''}
                        >
                            My Dashboard
                        </button>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginLeft: '10px', paddingLeft: '15px', borderLeft: '1px solid rgba(255,255,255,0.1)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <div style={{
                                    width: '30px',
                                    height: '30px',
                                    borderRadius: '50%',
                                    background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
                                    color: 'white',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontWeight: '700',
                                    fontSize: '13px',
                                    boxShadow: '0 2px 8px rgba(99, 102, 241, 0.3)'
                                }}>
                                    {user.name[0].toUpperCase()}
                                </div>
                                <span style={{ color: '#e2e8f0', fontSize: '13.5px', fontWeight: '600' }}>{user.name}</span>
                            </div>
                            <button onClick={handleLogout} className="logout-btn" style={{ padding: '8px 16px', fontSize: '13px' }}>
                                Logout
                            </button>
                        </div>
                    </>
                ) : (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginLeft: '10px', paddingLeft: '15px', borderLeft: '1px solid rgba(255,255,255,0.1)' }}>
                        <button
                            onClick={() => navigate('/login')}
                            className={currentView === '/login' ? 'active' : ''}
                            style={{ border: 'none', background: 'transparent' }}
                        >
                            Login
                        </button>
                        <button
                            onClick={() => navigate('/register')}
                            className={currentView === '/register' ? 'active' : ''}
                            style={{ background: 'linear-gradient(135deg, var(--color-primary), #4f46e5)', border: 'none', color: 'white' }}
                        >
                            Register
                        </button>
                    </div>
                )}

                <ThemeToggle theme={theme} toggleTheme={toggleTheme} />
            </div>
        </nav>
    );
};

export default Navbar;