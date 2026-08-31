import api from './api';

export const assessmentService = {
  async getAllAssessments() {
    const response = await api.get('/assessments');
    return response.data;
  },

  async getAssessment(id) {
    const response = await api.get(`/assessments/${id}`);
    return response.data;
  },

  async submitAssessment(id, answers) {
    const response = await api.post(`/assessments/${id}/submit`, { answers });
    return response.data;
  }
};
