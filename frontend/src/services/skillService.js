import api from './api';

export const skillService = {
  async getAllSkills() {
    const response = await api.get('/skills');
    return response.data;
  },

  async getSkillGaps() {
    const response = await api.get('/skills/gaps');
    return response.data;
  },

  async updateProficiency(skillName, proficiency) {
    const response = await api.put('/skills/proficiency', { skillName, proficiency });
    return response.data;
  }
};
