import { useState, useEffect } from 'react';
import { templatesApi } from '../api/templatesApi';

export function useTemplates(callType = null) {
  const [templates, setTemplates] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchTemplates();
  }, [callType]);

  const fetchTemplates = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      let response;
      if (callType) {
        response = await templatesApi.getTemplatesByCallType(callType);
      } else {
        response = await templatesApi.getAllTemplates();
      }
      
      setTemplates(response.data || []);
    } catch (err) {
      console.error('Failed to fetch templates:', err);
      setError(err.message || 'Failed to fetch templates');
    } finally {
      setIsLoading(false);
    }
  };

  const createTemplate = async (templateData) => {
    try {
      const response = await templatesApi.createTemplate(templateData);
      setTemplates(prev => [...prev, response.data]);
      return response.data;
    } catch (err) {
      console.error('Failed to create template:', err);
      throw err;
    }
  };

  const updateTemplate = async (id, templateData) => {
    try {
      const response = await templatesApi.updateTemplate(id, templateData);
      setTemplates(prev => 
        prev.map(template => 
          template.id === id ? response.data : template
        )
      );
      return response.data;
    } catch (err) {
      console.error('Failed to update template:', err);
      throw err;
    }
  };

  const deleteTemplate = async (id) => {
    try {
      await templatesApi.deleteTemplate(id);
      setTemplates(prev => prev.filter(template => template.id !== id));
    } catch (err) {
      console.error('Failed to delete template:', err);
      throw err;
    }
  };

  const cloneTemplate = async (id, newName) => {
    try {
      const response = await templatesApi.cloneTemplate(id, newName);
      setTemplates(prev => [...prev, response.data]);
      return response.data;
    } catch (err) {
      console.error('Failed to clone template:', err);
      throw err;
    }
  };

  const testTemplate = async (id, testVariables) => {
    try {
      const response = await templatesApi.testTemplate(id, testVariables);
      return response.data;
    } catch (err) {
      console.error('Failed to test template:', err);
      throw err;
    }
  };

  return {
    templates,
    isLoading,
    error,
    refetch: fetchTemplates,
    createTemplate,
    updateTemplate,
    deleteTemplate,
    cloneTemplate,
    testTemplate,
  };
}
