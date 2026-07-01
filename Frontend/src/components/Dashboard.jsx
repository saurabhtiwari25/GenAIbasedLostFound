import React, { useState, useEffect, useContext } from 'react';
import ItemService from '../services/itemService';
import NotificationService from '../services/notificationService';
import MessageService from '../services/messageService';
import Spinner from './Spinner';
import { AuthContext } from '../context/AuthContext';
import ItemCard from './ItemCard';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

const Dashboard = () => {
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [loadingNotif, setLoadingNotif] = useState(false);
    const [messages, setMessages] = useState([]);
    const [loadingMessages, setLoadingMessages] = useState(false);
    const [activeConversation, setActiveConversation] = useState(null);
    const [replyContents, setReplyContents] = useState({});

    const handleReplyChange = (convKey, value) => {
        setReplyContents(prev => ({ ...prev, [convKey]: value }));
    };

    useEffect(() => {
        if (!user || !user.id) {
            navigate('/login');
            return;
        }

        const fetchUserItems = async () => {
            setLoading(true);
            try {
                const response = await ItemService.getItemsByUser();
                setItems(response.data?.content || response.data || []);
            } catch (err) {
                console.error("Error fetching user items", err);
            } finally {
                setLoading(false);
            }
        };

        const fetchNotifications = async () => {
            setLoadingNotif(true);
            try {
                const response = await NotificationService.getNotifications();
                setNotifications(response.data);
            } catch (err) {
                console.error("Error fetching notifications", err);
            } finally {
                setLoadingNotif(false);
            }
        };

        const fetchMessages = async () => {
            setLoadingMessages(true);
            try {
                const response = await MessageService.getUserMessages();
                setMessages(response.data);
            } catch (err) {
                console.error("Error fetching messages", err);
            } finally {
                setLoadingMessages(false);
            }
        };

        fetchUserItems();
        fetchNotifications();
        fetchMessages();
    }, [user, navigate]);

    const handleMarkAllRead = async () => {
        try {
            await NotificationService.markAllNotificationsRead();
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
        } catch (err) {
            console.error("Error marking all as read", err);
        }
    };

    const handleMarkRead = async (notifId) => {
        try {
            await NotificationService.markNotificationRead(notifId);
            setNotifications(prev => prev.map(n => n.id === notifId ? { ...n, read: true } : n));
        } catch (err) {
            console.error("Error marking notification as read", err);
        }
    };

    const unreadCount = notifications.filter(n => !n.read).length;

    const conversations = {};
    messages.forEach(msg => {
        const otherUserId = msg.senderId === user.id ? msg.receiverId : msg.senderId;
        const otherUserName = msg.senderId === user.id ? msg.receiverName : msg.senderName;
        const key = `${msg.itemId}_${otherUserId}`;
        if (!conversations[key]) {
            conversations[key] = {
                key,
                itemId: msg.itemId,
                itemTitle: msg.itemTitle,
                otherUserId,
                otherUserName,
                messages: []
            };
        }
        conversations[key].messages.push(msg);
    });
    const conversationList = Object.values(conversations);

    const handleReplySubmit = async (e, conv) => {
        e.preventDefault();
        const content = replyContents[conv.key] || '';
        if (!content.trim()) return;
        
        try {
            const payload = {
                receiverId: conv.otherUserId,
                itemId: conv.itemId,
                content: content
            };
            const response = await MessageService.sendMessage(payload);
            setMessages(prev => [...prev, response.data]);
            setReplyContents(prev => ({ ...prev, [conv.key]: '' }));
        } catch (err) {
            console.error("Error sending reply", err);
            toast.error("Failed to send reply");
        }
    };

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <div>
                    <h2>My Dashboard</h2>
                    <p>Manage the items you have reported.</p>
                </div>
            </div>

            {notifications.length > 0 && (
                <div className="notif-panel">
                    <div className="notif-header">
                        <h3>
                            🔔 Notifications
                            {unreadCount > 0 && (
                                <span className="notif-badge">
                                    {unreadCount} new
                                </span>
                            )}
                        </h3>
                        {unreadCount > 0 && (
                            <button 
                                onClick={handleMarkAllRead}
                                className="link-text notif-mark-all-btn"
                            >
                                Mark all as read
                            </button>
                        )}
                    </div>
                    <div className="notif-list">
                        {notifications.map(notif => (
                            <div 
                                key={notif.id} 
                                onClick={() => !notif.read && handleMarkRead(notif.id)}
                                className={`notif-item ${notif.read ? 'read' : 'unread'}`}
                            >
                                <span className="notif-icon">
                                    {notif.read ? '📬' : '📩'}
                                </span>
                                <div className="notif-content">
                                    <p className={`notif-message ${notif.read ? '' : 'unread'}`}>
                                        {notif.message}
                                    </p>
                                    <p className="notif-time">
                                        {new Date(notif.createdAt).toLocaleString()}
                                    </p>
                                </div>
                                {!notif.read && (
                                    <span className="notif-dot" />
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <div className="dashboard-body">
                <div className="dashboard-main">
                    {loading ? (
                        <Spinner message="Loading your items..." />
                    ) : items.length === 0 ? (
                        <div className="empty-state">
                            <h3>You haven't reported any items yet.</h3>
                        </div>
                    ) : (
                        <div className="card-grid">
                            {items.map(item => (
                                <ItemCard 
                                    key={item.id} 
                                    item={item} 
                                    onClick={() => navigate(`/item/${item.id}`)}
                                />
                            ))}
                        </div>
                    )}
                </div>

                <div className="messages-panel">
                    <h3>✉️ Inbox ({conversationList.length})</h3>
                    {loadingMessages ? (
                        <Spinner message="Loading messages..." />
                    ) : conversationList.length === 0 ? (
                        <p className="muted">No messages yet.</p>
                    ) : (
                        <div className="conversation-list">
                            {conversationList.map(conv => (
                                <div key={conv.key} className="conversation-item">
                                    <div 
                                        className="conversation-header" 
                                        onClick={() => setActiveConversation(activeConversation === conv.key ? null : conv.key)}
                                    >
                                        <strong>{conv.otherUserName}</strong> regarding <em>{conv.itemTitle}</em>
                                        <span>{activeConversation === conv.key ? '▲' : '▼'}</span>
                                    </div>
                                    {activeConversation === conv.key && (
                                        <div className="conversation-body">
                                            <div className="message-history">
                                                {conv.messages.map(msg => (
                                                    <div key={msg.id} className={`message-bubble ${msg.senderId === user.id ? 'sent' : 'received'}`}>
                                                        <span className="message-sender">{msg.senderId === user.id ? 'You' : msg.senderName}</span>
                                                        <p>{msg.content}</p>
                                                        <span className="message-time">{new Date(msg.createdAt).toLocaleString()}</span>
                                                    </div>
                                                ))}
                                            </div>
                                            <form className="message-reply-form" onSubmit={(e) => handleReplySubmit(e, conv)}>
                                                <input 
                                                    type="text" 
                                                    placeholder="Type your reply..." 
                                                    value={replyContents[conv.key] || ''}
                                                    onChange={(e) => handleReplyChange(conv.key, e.target.value)}
                                                />
                                                <button type="submit" disabled={!(replyContents[conv.key] || '').trim()}>Send</button>
                                            </form>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
