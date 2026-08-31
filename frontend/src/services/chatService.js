import api from './api';

export const chatService = {
  async getChatHistory() {
    const response = await api.get('/chat/history');
    return response.data;
  },

  async sendMessage(message) {
    const response = await api.post('/chat', { message });
    return response.data;
  }
};
