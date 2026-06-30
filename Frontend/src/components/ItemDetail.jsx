import React, { useState, useEffect, useContext } from 'react';
import ItemService from '../services/itemService';
import { AuthContext } from '../context/AuthContext';
import ItemCard from './ItemCard';
import CommentSection from './CommentSection';
import MessageService from '../services/messageService';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

const ItemDetail = () => {
    const { id } = useParams();
    const itemId = id;
    const navigate = useNavigate();
    const [item, setItem] = useState(null);
    const { user } = useContext(AuthContext);
    const [loading, setLoading] = useState(false);
    const [matches, setMatches] = useState(null);
    const [loadingMatches, setLoadingMatches] = useState(false);

    // Edit Modal State
    const [showEditModal, setShowEditModal] = useState(false);
    const [editForm, setEditForm] = useState({ title: '', description: '', location: '', contactInfo: '' });

    const [showFoundModal, setShowFoundModal] = useState(false);
    const [foundForm, setFoundForm] = useState({ foundLocation: '', founderName: '', founderContactInfo: '' });

    const [showMsgModal, setShowMsgModal] = useState(false);
    const [msgContent, setMsgContent] = useState('');

    useEffect(() => {
        loadItem();
        setMatches(null);
    }, [itemId]);

    const loadItem = async () => {
        try {
            const response = await ItemService.getItemById(itemId);
            setItem(response.data);
        } catch (err) {
            console.error("Could not load item details", err);
        }
    };

    const openEditModal = () => {
        setEditForm({
            title: item.title,
            description: item.description,
            location: item.location,
            contactInfo: item.contactInfo || ''
        });
        setShowEditModal(true);
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditForm(prev => ({ ...prev, [name]: value }));
    };

    const handleEditSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = { ...editForm, found: item.found, reporterId: user.id };
            const response = await ItemService.updateItem(item.id, payload);
            setItem(response.data);
            setShowEditModal(false);
            toast.success("Item updated successfully!");
        } catch (err) {
            console.error("Error updating item", err);
            toast.error(err.response?.data?.message || "Failed to update item. You may not have permission.");
        } finally {
            setLoading(false);
        }
    };

    const handleFoundChange = (e) => {
        const { name, value } = e.target;
        setFoundForm(prev => ({ ...prev, [name]: value }));
    };

    const handleFoundSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = { ...foundForm, finderUserId: user.id };
            const response = await ItemService.markAsFound(item.id, payload);
            setItem(response.data);
            setShowFoundModal(false);
            toast.success("Thank you! The item reporter has been notified.");
            navigate('/');
        } catch (err) {
            console.error("Error marking item as found", err);
            toast.error(err.response?.data?.message || "Failed to mark as found.");
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteItem = async () => {
        if (!window.confirm("Are you sure you want to delete this item?")) return;
        setLoading(true);
        try {
            await ItemService.deleteItem(itemId);
            toast.success("Item deleted successfully!");
            navigate('/');
        } catch (err) {
            console.error("Error deleting item", err);
            toast.error("Failed to delete item. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleMessageSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                senderId: user.id,
                receiverId: item.reporterId,
                itemId: item.id,
                content: msgContent
            };
            await MessageService.sendMessage(payload);
            setShowMsgModal(false);
            setMsgContent('');
            toast.success("Message sent successfully!");
        } catch (err) {
            console.error("Error sending message", err);
            toast.error("Failed to send message.");
        } finally {
            setLoading(false);
        }
    };

    const handleFindMatches = async () => {
        if (!item) return;
        setLoadingMatches(true);
        try {
            const response = await ItemService.getSmartMatches(itemId);
            setMatches(response.data);
        } catch (err) {
            console.error("Error finding smart matches", err);
            toast.error("Failed to fetch smart matches. Please try again.");
        } finally {
            setLoadingMatches(false);
        }
    };

    if (!item) return <div className="container">Loading...</div>;

    const isReporter = user && user.id == item.reporterId;
    const canMarkFound = user && !isReporter && !item.found;

    return (
        <>
            <div className="form-card detail-card">
                <button onClick={() => navigate(-1)} className="link-text back-btn">
                    ← Back
                </button>
                
                <div className="detail-header">
                    <h2>{item.title}</h2>
                    <span className={`detail-status-badge ${item.found ? 'found' : 'lost'}`}>
                        {item.found ? " Found" : " Lost"}
                    </span>
                </div>

                <div className="detail-meta">
                    <p><strong>📍 Location Lost:</strong> {item.location}</p>
                    {item.contactInfo && (
                        <p><strong> Reporter Contact:</strong> {item.contactInfo}</p>
                    )}
                    <p className="muted"><strong>📅 Reported on:</strong> {new Date(item.createdAt).toLocaleDateString()}</p>
                    {item.reporterName && (
                        <p><strong> Reported by:</strong> {item.reporterName}</p>
                    )}
                </div>
                
                <p><strong> Description:</strong></p>
                <div className="description-block">
                    {item.description}
                </div>
                
                {item.imagePath && (
                    <div className="detail-image-wrap">
                        <strong>Attached Photo:</strong>
                        <div className="detail-image-frame">
                            <img
                                src={item.imagePath}
                                alt={item.title}
                                style={{ maxWidth: '60%', height: 'auto', borderRadius: '12px', marginTop: '12px' }}
                            />
                        </div>
                    </div>
                )}

                {item.found && item.founderName && (
                    <div className="found-details-box">
                        <h4> Found Details</h4>
                        <p><strong>Found by:</strong> {item.founderName}</p>
                        <p><strong>Found at:</strong> {item.foundLocation}</p>
                        <p><strong>Contact:</strong> {item.founderContactInfo}</p>
                    </div>
                )}

                {user && (
                    <div className="detail-actions">
                        {isReporter && (
                            <button 
                                onClick={openEditModal} 
                                className="primary-btn btn-edit" 
                            >
                                ✏️ Edit Item
                            </button>
                        )}
                        {canMarkFound && (
                            <button 
                                onClick={() => setShowFoundModal(true)} 
                                className="primary-btn btn-found" 
                            >
                                🔍 I Found This!
                            </button>
                        )}
                        {user && !isReporter && (
                            <button 
                                onClick={() => setShowMsgModal(true)} 
                                className="primary-btn btn-message" 
                            >
                                 Message Owner
                            </button>
                        )}
                        {isReporter && (
                            <button 
                                onClick={handleDeleteItem} 
                                disabled={loading}
                                className="primary-btn btn-delete" 
                            >
                                Delete
                            </button>
                        )}
                    </div>
                )}

                {isReporter && (
                    <div className="smart-match-section">
                        <div className="smart-match-header">
                            <div>
                                <h3> Smart Matching</h3>
                                <p>{item.found ? "Let AI find potential lost item matches." : "Let AI find potential matches for your lost item."}</p>
                            </div>
                            <button 
                                onClick={handleFindMatches} 
                                disabled={loadingMatches}
                                className="primary-btn btn-ai" 
                            >
                                {loadingMatches ? 'Searching...' : 'Find Matches (AI)'}
                            </button>
                        </div>

                        {matches && (
                            <div className="smart-match-results">
                                {matches.length === 0 ? (
                                    <div className="smart-match-empty">
                                        No probable matches found by AI at this time.
                                    </div>
                                ) : (
                                    <div>
                                        <h4>Suggested Matches ({matches.length})</h4>
                                        <div className="smart-match-grid">
                                            {matches.map(matchItem => (
                                                <ItemCard 
                                                    key={matchItem.id} 
                                                    item={matchItem} 
                                                    onClick={() => navigate(`/item/${matchItem.id}`)}
                                                />
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
                
                {/* ─── Comment Section ─── */}
                <CommentSection itemId={item.id} />
                
            </div>

            {/* ─── Edit Modal ─── */}
            {showEditModal && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>✏️ Edit Item</h3>
                            <button className="modal-close" onClick={() => setShowEditModal(false)}>✕</button>
                        </div>
                        <form onSubmit={handleEditSubmit}>
                            <div className="input-group">
                                <label>Title</label>
                                <input name="title" value={editForm.title} onChange={handleEditChange} required />
                            </div>
                            <div className="input-group">
                                <label>Description</label>
                                <textarea name="description" rows="3" value={editForm.description} onChange={handleEditChange} required />
                            </div>
                            <div className="input-group">
                                <label>Location Lost</label>
                                <input name="location" value={editForm.location} onChange={handleEditChange} required />
                            </div>
                            <div className="input-group">
                                <label>Your Contact Number</label>
                                <input name="contactInfo" placeholder="Phone number" value={editForm.contactInfo} onChange={handleEditChange} required />
                            </div>
                            <div className="modal-actions">
                                <button type="submit" className="primary-btn" disabled={loading}>
                                    {loading ? 'Saving...' : 'Save Changes'}
                                </button>
                                <button type="button" onClick={() => setShowEditModal(false)} className="primary-btn btn-cancel">
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ─── I Found This Modal ─── */}
            {showFoundModal && (
                <div className="modal-overlay" onClick={() => setShowFoundModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3> I Found This Item!</h3>
                            <button className="modal-close" onClick={() => setShowFoundModal(false)}>✕</button>
                        </div>
                        <p className="modal-subtitle">
                            Please provide your details so the owner can contact you.
                        </p>
                        <form onSubmit={handleFoundSubmit}>
                            <div className="input-group">
                                <label>Where did you find it?</label>
                                <input name="foundLocation" placeholder="e.g., Library 2nd floor" value={foundForm.foundLocation} onChange={handleFoundChange} required />
                            </div>
                            <div className="input-group">
                                <label>Your Name</label>
                                <input name="founderName" placeholder="Enter your name" value={foundForm.founderName} onChange={handleFoundChange} required />
                            </div>
                            <div className="input-group">
                                <label>Your Contact Number</label>
                                <input name="founderContactInfo" placeholder="Phone number" value={foundForm.founderContactInfo} onChange={handleFoundChange} required />
                            </div>
                            <div className="modal-actions">
                                <button type="submit" className="primary-btn btn-submit-found" disabled={loading}>
                                    {loading ? 'Submitting...' : 'Submit & Notify Owner'}
                                </button>
                                <button type="button" onClick={() => setShowFoundModal(false)} className="primary-btn btn-cancel">
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ─── Message Owner Modal ─── */}
            {showMsgModal && (
                <div className="modal-overlay" onClick={() => setShowMsgModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>✉️ Message Owner</h3>
                            <button className="modal-close" onClick={() => setShowMsgModal(false)}>✕</button>
                        </div>
                        <p className="modal-subtitle">
                            Send a private message to the item reporter.
                        </p>
                        <form onSubmit={handleMessageSubmit}>
                            <div className="input-group">
                                <label>Message</label>
                                <textarea 
                                    name="msgContent" 
                                    rows="4" 
                                    placeholder="I think I found your item..." 
                                    value={msgContent} 
                                    onChange={(e) => setMsgContent(e.target.value)} 
                                    required 
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="submit" className="primary-btn btn-message" disabled={loading || !msgContent.trim()}>
                                    {loading ? 'Sending...' : 'Send Message'}
                                </button>
                                <button type="button" onClick={() => setShowMsgModal(false)} className="primary-btn btn-cancel">
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
};

export default ItemDetail;
