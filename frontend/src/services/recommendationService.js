import api from './api';

export const recommendationService = {
  async getRecommendations() {
    const response = await api.get('/recommendations');
    return response.data;
  },

  async submitFeedback(recommendationId, rating, feedbackText) {
    const response = await api.post(`/recommendations/${recommendationId}/feedback`, { rating, feedbackText });
    return response.data;
  }
};
