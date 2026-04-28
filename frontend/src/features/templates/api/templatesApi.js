import { apiClient } from '../../../lib/api-client';

export const templatesApi = {
  // Get all templates
  getAllTemplates: (params = {}) => {
    return apiClient.get('/templates', { params });
  },

  // Get template by ID
  getTemplate: (id) => {
    return apiClient.get(`/templates/${id}`);
  },

  // Create template
  createTemplate: (templateData) => {
    return apiClient.post('/templates', templateData);
  },

  // Update template
  updateTemplate: (id, templateData) => {
    return apiClient.put(`/templates/${id}`, templateData);
  },

  // Delete template
  deleteTemplate: (id) => {
    return apiClient.delete(`/templates/${id}`);
  },

  // Clone template
  cloneTemplate: (id, newName) => {
    const params = newName ? { new_name: newName } : {};
    return apiClient.post(`/templates/${id}/clone`, null, { params });
  },

  // Get templates by call type
  getTemplatesByCallType: (callType) => {
    return apiClient.get(`/templates/by-type/${callType}`);
  },

  // Get public templates
  getPublicTemplates: (callType = null) => {
    const params = callType ? { call_type: callType } : {};
    return apiClient.get('/templates/public', { params });
  },

  // Search templates
  searchTemplates: (query) => {
    return apiClient.get('/templates/search', { params: { query } });
  },

  // Get template statistics
  getTemplateStatistics: () => {
    return apiClient.get('/templates/statistics');
  },

  // Get most used templates
  getMostUsedTemplates: (limit = 10) => {
    return apiClient.get('/templates/most-used', { params: { limit } });
  },

  // Test template configuration
  testTemplate: (id, testVariables) => {
    return apiClient.post(`/templates/${id}/test`, testVariables);
  },
};
