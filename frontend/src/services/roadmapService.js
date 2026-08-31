import api from './api';

export const roadmapService = {
  async getRoadmap() {
    const response = await api.get('/roadmap');
    return response.data;
  },

  async generateRoadmap() {
    const response = await api.post('/roadmap/generate');
    return response.data;
  },

  async updateItemStatus(itemId, status) {
    const response = await api.put(`/roadmap/items/${itemId}/status`, { status });
    return response.data;
  },

  async recalculateTimeline(weeklyHours) {
    const response = await api.post('/roadmap/recalculate-time', { weeklyHours });
    return response.data;
  }
};
