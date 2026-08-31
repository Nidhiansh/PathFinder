import api from './api';

export const profileService = {
  async getProfile() {
    const response = await api.get('/profile');
    return response.data;
  },

  async updateProfile(profileData) {
    const response = await api.put('/profile', profileData);
    return response.data;
  },

  async extractGoal(prompt) {
    const response = await api.post('/profile/extract-goal', { prompt });
    return response.data;
  }
};
