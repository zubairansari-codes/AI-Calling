import React, { useState } from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
  Loader2,
  Plus,
  Copy,
  Edit,
  Trash2,
  Play,
  Search,
  Filter,
  Star,
  Users,
  Settings,
} from "lucide-react";
import { useTemplates } from '../hooks/useTemplates';

const CALL_TYPES = [
  { value: 'DSC_COLLECTION', label: 'DSC Collection' },
  { value: 'PAYMENT_REMINDER', label: 'Payment Reminder' },
  { value: 'DELIVERY_CONFIRMATION', label: 'Delivery Confirmation' },
  { value: 'COMPLAINT_RESOLUTION', label: 'Complaint Resolution' },
  { value: 'EMERGENCY_RESPONSE', label: 'Emergency Response' },
  { value: 'SATISFACTION_SURVEY', label: 'Satisfaction Survey' },
  { value: 'CUSTOM', label: 'Custom' },
];

const LANGUAGES = [
  { value: 'hi', label: 'Hindi' },
  { value: 'en', label: 'English' },
  { value: 'ta', label: 'Tamil' },
  { value: 'te', label: 'Telugu' },
  { value: 'bn', label: 'Bengali' },
];

export function TemplateManager() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCallType, setSelectedCallType] = useState('');
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState(null);
  const [testVariables, setTestVariables] = useState({});
  const [testResults, setTestResults] = useState(null);

  const {
    templates,
    isLoading,
    error,
    createTemplate,
    updateTemplate,
    deleteTemplate,
    cloneTemplate,
    testTemplate,
  } = useTemplates();

  const filteredTemplates = templates?.filter(template => {
    const matchesSearch = !searchQuery || 
      template.templateName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      template.description?.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesCallType = !selectedCallType || template.callType === selectedCallType;
    
    return matchesSearch && matchesCallType;
  }) || [];

  const handleCreateTemplate = async (templateData) => {
    try {
      await createTemplate(templateData);
      setShowCreateDialog(false);
    } catch (error) {
      console.error('Failed to create template:', error);
    }
  };

  const handleUpdateTemplate = async (templateData) => {
    try {
      await updateTemplate(editingTemplate.id, templateData);
      setEditingTemplate(null);
    } catch (error) {
      console.error('Failed to update template:', error);
    }
  };

  const handleDeleteTemplate = async (id) => {
    if (window.confirm('Are you sure you want to delete this template?')) {
      try {
        await deleteTemplate(id);
      } catch (error) {
        console.error('Failed to delete template:', error);
      }
    }
  };

  const handleCloneTemplate = async (id) => {
    try {
      await cloneTemplate(id);
    } catch (error) {
      console.error('Failed to clone template:', error);
    }
  };

  const handleTestTemplate = async (id) => {
    try {
      const results = await testTemplate(id, testVariables);
      setTestResults(results);
    } catch (error) {
      console.error('Failed to test template:', error);
      setTestResults({ valid: false, error: error.message });
    }
  };

  const getCallTypeLabel = (callType) => {
    return CALL_TYPES.find(type => type.value === callType)?.label || callType;
  };

  const getLanguageLabel = (language) => {
    return LANGUAGES.find(lang => lang.value === language)?.label || language;
  };

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Template Manager</h1>
          <p className="text-muted-foreground mt-2">
            Create and manage AI agent templates for different call types
          </p>
        </div>
        <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create Template
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>Create New Template</DialogTitle>
              <DialogDescription>
                Create a new AI agent template for automated calls
              </DialogDescription>
            </DialogHeader>
            <TemplateForm
              onSubmit={handleCreateTemplate}
              onCancel={() => setShowCreateDialog(false)}
            />
          </DialogContent>
        </Dialog>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search templates..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={selectedCallType} onValueChange={setSelectedCallType}>
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Filter by type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Types</SelectItem>
                {CALL_TYPES.map((type) => (
                  <SelectItem key={type.value} value={type.value}>
                    {type.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Templates Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      ) : error ? (
        <Alert>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredTemplates.map((template) => (
            <Card key={template.id} className="relative">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="text-lg">{template.templateName}</CardTitle>
                    <CardDescription className="mt-1">
                      {template.description}
                    </CardDescription>
                  </div>
                  <div className="flex gap-1">
                    {template.publicTemplate && (
                      <Badge variant="secondary" className="text-xs">
                        <Users className="h-3 w-3 mr-1" />
                        Public
                      </Badge>
                    )}
                    {template.usageCount > 0 && (
                      <Badge variant="outline" className="text-xs">
                        <Star className="h-3 w-3 mr-1" />
                        {template.usageCount}
                      </Badge>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center gap-2">
                    <Badge variant="outline">
                      {getCallTypeLabel(template.callType)}
                    </Badge>
                    <Badge variant="outline">
                      {getLanguageLabel(template.language)}
                    </Badge>
                  </div>
                  
                  <div className="text-sm text-muted-foreground">
                    <p><strong>Agent:</strong> {template.agentName}</p>
                    <p><strong>Hinglish:</strong> {template.hinglishMode ? 'Yes' : 'No'}</p>
                    {template.successRate && (
                      <p><strong>Success Rate:</strong> {template.successRate.toFixed(1)}%</p>
                    )}
                  </div>

                  {template.firstMessage && (
                    <div className="p-2 bg-muted rounded text-sm">
                      <p className="font-medium mb-1">First Message:</p>
                      <p className="text-muted-foreground truncate">
                        {template.firstMessage}
                      </p>
                    </div>
                  )}
                </div>
              </CardContent>
              <CardFooter className="flex justify-between">
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleCloneTemplate(template.id)}
                  >
                    <Copy className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setEditingTemplate(template)}
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleDeleteTemplate(template.id)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleTestTemplate(template.id)}
                >
                  <Play className="h-4 w-4" />
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}

      {/* Edit Template Dialog */}
      <Dialog open={!!editingTemplate} onOpenChange={() => setEditingTemplate(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Edit Template</DialogTitle>
            <DialogDescription>
              Update the AI agent template configuration
            </DialogDescription>
          </DialogHeader>
          {editingTemplate && (
            <TemplateForm
              template={editingTemplate}
              onSubmit={handleUpdateTemplate}
              onCancel={() => setEditingTemplate(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Test Results Dialog */}
      {testResults && (
        <Dialog open={!!testResults} onOpenChange={() => setTestResults(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Template Test Results</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              {testResults.valid ? (
                <div className="space-y-4">
                  <Alert>
                    <AlertDescription>
                      Template configuration is valid!
                    </AlertDescription>
                  </Alert>
                  
                  {testResults.agentConfig && (
                    <div>
                      <h4 className="font-medium mb-2">Agent Configuration:</h4>
                      <pre className="text-sm bg-muted p-3 rounded overflow-auto">
                        {JSON.stringify(testResults.agentConfig, null, 2)}
                      </pre>
                    </div>
                  )}
                  
                  {testResults.processedFirstMessage && (
                    <div>
                      <h4 className="font-medium mb-2">Processed First Message:</h4>
                      <p className="text-sm bg-muted p-3 rounded">
                        {testResults.processedFirstMessage}
                      </p>
                    </div>
                  )}
                </div>
              ) : (
                <Alert variant="destructive">
                  <AlertDescription>
                    Template test failed: {testResults.error}
                  </AlertDescription>
                </Alert>
              )}
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}

function TemplateForm({ template, onSubmit, onCancel }) {
  const [formData, setFormData] = useState({
    templateName: template?.templateName || '',
    callType: template?.callType || '',
    description: template?.description || '',
    agentName: template?.agentName || 'Assistant',
    language: template?.language || 'hi',
    hinglishMode: template?.hinglishMode ?? true,
    systemPrompt: template?.systemPrompt || '',
    firstMessage: template?.firstMessage || '',
    maxDurationSeconds: template?.maxDurationSeconds || 300,
    maxRetries: template?.maxRetries || 3,
    active: template?.active ?? true,
    publicTemplate: template?.publicTemplate ?? false,
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="templateName">Template Name</Label>
          <Input
            id="templateName"
            value={formData.templateName}
            onChange={(e) => handleChange('templateName', e.target.value)}
            required
          />
        </div>
        <div>
          <Label htmlFor="callType">Call Type</Label>
          <Select
            value={formData.callType}
            onValueChange={(value) => handleChange('callType', value)}
            required
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {CALL_TYPES.map((type) => (
                <SelectItem key={type.value} value={type.value}>
                  {type.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div>
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          value={formData.description}
          onChange={(e) => handleChange('description', e.target.value)}
          rows={2}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="agentName">Agent Name</Label>
          <Input
            id="agentName"
            value={formData.agentName}
            onChange={(e) => handleChange('agentName', e.target.value)}
          />
        </div>
        <div>
          <Label htmlFor="language">Language</Label>
          <Select
            value={formData.language}
            onValueChange={(value) => handleChange('language', value)}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {LANGUAGES.map((lang) => (
                <SelectItem key={lang.value} value={lang.value}>
                  {lang.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div>
        <Label htmlFor="systemPrompt">System Prompt</Label>
        <Textarea
          id="systemPrompt"
          value={formData.systemPrompt}
          onChange={(e) => handleChange('systemPrompt', e.target.value)}
          rows={4}
          required
        />
      </div>

      <div>
        <Label htmlFor="firstMessage">First Message</Label>
        <Textarea
          id="firstMessage"
          value={formData.firstMessage}
          onChange={(e) => handleChange('firstMessage', e.target.value)}
          rows={2}
          required
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <Label htmlFor="maxDurationSeconds">Max Duration (seconds)</Label>
          <Input
            id="maxDurationSeconds"
            type="number"
            value={formData.maxDurationSeconds}
            onChange={(e) => handleChange('maxDurationSeconds', parseInt(e.target.value))}
          />
        </div>
        <div>
          <Label htmlFor="maxRetries">Max Retries</Label>
          <Input
            id="maxRetries"
            type="number"
            value={formData.maxRetries}
            onChange={(e) => handleChange('maxRetries', parseInt(e.target.value))}
          />
        </div>
      </div>

      <div className="flex items-center space-x-4">
        <label className="flex items-center space-x-2">
          <input
            type="checkbox"
            checked={formData.hinglishMode}
            onChange={(e) => handleChange('hinglishMode', e.target.checked)}
          />
          <span>Enable Hinglish Mode</span>
        </label>
        <label className="flex items-center space-x-2">
          <input
            type="checkbox"
            checked={formData.active}
            onChange={(e) => handleChange('active', e.target.checked)}
          />
          <span>Active</span>
        </label>
        <label className="flex items-center space-x-2">
          <input
            type="checkbox"
            checked={formData.publicTemplate}
            onChange={(e) => handleChange('publicTemplate', e.target.checked)}
          />
          <span>Public Template</span>
        </label>
      </div>

      <DialogFooter>
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          {template ? 'Update' : 'Create'} Template
        </Button>
      </DialogFooter>
    </form>
  );
}
