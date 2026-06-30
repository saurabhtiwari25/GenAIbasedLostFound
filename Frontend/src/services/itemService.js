import axios from '../api/axiosConfig';

const ItemService = {

    getAllItems: async () => {
        return await axios.get(`/items`);
    },

    getItemsByUser: async (userId) => {
        return await axios.get(`/items/user/${userId}`);
    },

    getSmartMatches: async (itemId) => {
        return await axios.get(`/items/${itemId}/smart-matches`);
    },

    createItem: async (itemData) => {
        return await axios.post('/items', itemData);
    },

    uploadImage: async (id, file) => {
        const formData = new FormData();
        formData.append('file', file);
        
        return await axios.post(`/items/${id}/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    getItemById: async (id) => {
        return await axios.get(`/items/${id}`);
    },

    deleteItem: async (id) => {
        return await axios.delete(`/items/${id}`);
    },

    updateItem: async (id, itemData) => {
        return await axios.put(`/items/${id}`, itemData);
    },

    searchItems: async (keyword) => {
        return await axios.get(`/items/search?keyword=${keyword}`);
    },

    markAsFound: async (id, foundData) => {
        return await axios.post(`/items/${id}/found`, foundData);
    },

    resolveItem: async (id) => {
        return await axios.post(`/items/${id}/resolve`);
    },

    confirmMatch: async (sourceId, matchId) => {
        return await axios.post(`/items/${sourceId}/confirm-match/${matchId}`);
    }
};

export default ItemService;