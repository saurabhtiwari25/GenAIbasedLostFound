import axios from '../api/axiosConfig';

const API_URL = '/messages';

const MessageService = {
    sendMessage: (messageData) => {
        return axios.post(API_URL, messageData);
    },

    getUserMessages: () => {
        return axios.get(`${API_URL}/me`);
    }
};

export default MessageService;
