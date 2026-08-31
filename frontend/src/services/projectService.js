import api from './api';

export const projectService = {
  async getProjects() {
    const response = await api.get('/projects');
    return response.data;
  },

  async generateAdaptiveProjects(topic) {
    const response = await api.post('/projects/generate', { topic });
    return response.data;
  },

  async submitProject(projectId, submissionData) {
    const response = await api.post(`/projects/${projectId}/submit`, submissionData);
    return response.data;
  }
};
