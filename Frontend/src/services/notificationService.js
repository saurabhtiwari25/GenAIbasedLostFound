import axios from '../api/axiosConfig';

const NotificationService = {
    getNotifications: async (userId) => {
        return await axios.get(`/notifications/user/${userId}`);
    },

    markNotificationRead: async (notificationId, userId) => {
        return await axios.put(`/notifications/${notificationId}/read?userId=${userId}`);
    },

    markAllNotificationsRead: async (userId) => {
        return await axios.put(`/notifications/user/${userId}/read-all`);
    }
};

export default NotificationService;
