import axios from '../api/axiosConfig';

const CommentService = {
    getCommentsByItemId: async (itemId) => {
        return await axios.get(`/comments/item/${itemId}`);
    },

    addComment: async (itemId, commentData) => {
        return await axios.post(`/comments/item/${itemId}`, commentData);
    }
};

export default CommentService;
