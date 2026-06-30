import axios from 'axios';

const API_URL = '/messages';

class MessageService {
    sendMessage(messageData) {
        return axios.post(API_URL, messageData);
    }

    getUserMessages(userId) {
        return axios.get(`${API_URL}/user/${userId}`);
    }
}

export default new MessageService();
