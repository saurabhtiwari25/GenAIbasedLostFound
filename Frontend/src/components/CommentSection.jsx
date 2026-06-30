import React, { useState, useEffect, useContext } from 'react';
import CommentService from '../services/commentService';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-hot-toast';

const CommentSection = ({ itemId }) => {
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [loading, setLoading] = useState(false);
    const { user } = useContext(AuthContext);

    useEffect(() => {
        if (itemId) {
            loadComments();
        }
    }, [itemId]);

    const loadComments = async () => {
        try {
            const response = await CommentService.getCommentsByItemId(itemId);
            setComments(response.data);
        } catch (error) {
            console.error("Failed to load comments", error);
        }
    };

    const handleCommentSubmit = async (e) => {
        e.preventDefault();
        if (!newComment.trim()) return;

        setLoading(true);
        try {
            const payload = {
                content: newComment,
                authorId: user.id
            };
            const response = await CommentService.addComment(itemId, payload);
            setComments([...comments, response.data]);
            setNewComment('');
        } catch (error) {
            console.error("Failed to add comment", error);
            toast.error("Failed to add comment. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="comment-section">
            <h3>💬 Comments</h3>
            
            <div className="comment-list">
                {comments.length === 0 ? (
                    <div className="comment-empty">
                        No comments yet. Be the first to comment!
                    </div>
                ) : (
                    comments.map(comment => (
                        <div key={comment.id} className="comment-item">
                            <div className="comment-header">
                                <strong className="comment-author">{comment.authorName}</strong>
                                <span className="comment-date">
                                    {new Date(comment.createdAt).toLocaleString()}
                                </span>
                            </div>
                            <p className="comment-text">
                                {comment.content}
                            </p>
                        </div>
                    ))
                )}
            </div>

            {user ? (
                <form onSubmit={handleCommentSubmit} className="comment-form">
                    <textarea 
                        placeholder="Write a comment..." 
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        rows="3"
                    />
                    <button 
                        type="submit" 
                        disabled={loading || !newComment.trim()} 
                        className="primary-btn"
                    >
                        {loading ? 'Posting...' : 'Post Comment'}
                    </button>
                </form>
            ) : (
                <div className="comment-login-prompt">
                    Please log in to add a comment.
                </div>
            )}
        </div>
    );
};

export default CommentSection;
