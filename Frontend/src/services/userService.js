import axios from '../api/axiosConfig';

const UserService = {

    register: async (userData) => {
        return await axios.post('/users/register', userData);
    },

    login: async (credentials) => {
        return await axios.post('/users/login', credentials);
    }
};

export default UserService;