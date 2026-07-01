import axios from '../api/axiosConfig';

const NotificationService = {
    getNotifications: async () => {
        return await axios.get(`/notifications/me`);
    },

    markNotificationRead: async (notificationId) => {
        return await axios.put(`/notifications/${notificationId}/read`);
    },

    markAllNotificationsRead: async () => {
        return await axios.put(`/notifications/me/read-all`);
    }
};

export default NotificationService;
