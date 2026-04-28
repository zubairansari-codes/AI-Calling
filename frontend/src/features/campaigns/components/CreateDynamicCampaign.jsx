import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Loader2, Plus, HelpCircle, Copy, Settings } from "lucide-react";
import { useCreateCampaign } from '../hooks/useCreateCampaign';
import { useTemplates } from '../../templates/hooks/useTemplates';

const CALL_TYPES = [
  { value: 'DSC_COLLECTION', label: 'DSC Collection', description: 'Collect delivery service codes from customers' },
  { value: 'PAYMENT_REMINDER', label: 'Payment Reminder', description: 'Remind customers about pending payments' },
  { value: 'DELIVERY_CONFIRMATION', label: 'Delivery Confirmation', description: 'Confirm successful deliveries' },
  { value: 'COMPLAINT_RESOLUTION', label: 'Complaint Resolution', description: 'Handle customer complaints' },
  { value: 'EMERGENCY_RESPONSE', label: 'Emergency Response', description: 'Handle emergency situations' },
  { value: 'SATISFACTION_SURVEY', label: 'Satisfaction Survey', description: 'Conduct customer satisfaction surveys' },
  { value: 'CUSTOM', label: 'Custom', description: 'Create your own custom calling campaign' },
];

const CALL_TYPE_CONFIGS = {
  DSC_COLLECTION: {
    defaultName: 'DSC Collection Campaign',
    description: 'Automated calls to collect delivery service codes from customers',
    icon: '📋',
    color: 'blue',
    fields: ['deliveryDate'],
  },
  PAYMENT_REMINDER: {
    defaultName: 'Payment Reminder Campaign',
    description: 'Automated calls to remind customers about pending payments',
    icon: '💰',
    color: 'green',
    fields: ['paymentAmount', 'dueDate'],
  },
  DELIVERY_CONFIRMATION: {
    defaultName: 'Delivery Confirmation Campaign',
    description: 'Automated calls to confirm successful deliveries',
    icon: '✅',
    color: 'emerald',
    fields: ['deliveryDate', 'deliveryTime'],
  },
  COMPLAINT_RESOLUTION: {
    defaultName: 'Complaint Resolution Campaign',
    description: 'Automated calls to handle and resolve customer complaints',
    icon: '🔧',
    color: 'orange',
    fields: ['complaintCategory'],
  },
  EMERGENCY_RESPONSE: {
    defaultName: 'Emergency Response Campaign',
    description: 'Automated calls to handle emergency situations',
    icon: '🚨',
    color: 'red',
    fields: ['emergencyType'],
  },
  SATISFACTION_SURVEY: {
    defaultName: 'Satisfaction Survey Campaign',
    description: 'Automated calls to conduct customer satisfaction surveys',
    icon: '📊',
    color: 'purple',
    fields: ['surveyQuestions'],
  },
  CUSTOM: {
    defaultName: 'Custom Campaign',
    description: 'Create your own custom calling campaign',
    icon: '🎯',
    color: 'gray',
    fields: ['customPurpose'],
  },
};

export function CreateDynamicCampaign() {
  const navigate = useNavigate();
  const [selectedCallType, setSelectedCallType] = useState('');
  const [selectedTemplate, setSelectedTemplate] = useState('');
  const [campaignName, setCampaignName] = useState('');
  const [campaignDescription, setCampaignDescription] = useState('');
  const [campaignConfig, setCampaignConfig] = useState({});
  const [csvFile, setCsvFile] = useState(null);
  const [errors, setErrors] = useState({});

  const { createCampaign, isLoading } = useCreateCampaign();
  const { templates, isLoading: templatesLoading } = useTemplates(selectedCallType);

  const callTypeConfig = CALL_TYPE_CONFIGS[selectedCallType];

  useEffect(() => {
    if (selectedCallType && callTypeConfig) {
      setCampaignName(callTypeConfig.defaultName);
      setCampaignDescription(callTypeConfig.description);
      setCampaignConfig(getDefaultConfig(selectedCallType));
    }
  }, [selectedCallType]);

  const getDefaultConfig = (callType) => {
    const config = {};
    
    switch (callType) {
      case 'DSC_COLLECTION':
        config.deliveryDate = new Date().toISOString().split('T')[0];
        break;
      case 'PAYMENT_REMINDER':
        config.paymentAmount = '850';
        config.dueDate = new Date().toISOString().split('T')[0];
        break;
      case 'DELIVERY_CONFIRMATION':
        config.deliveryDate = new Date().toISOString().split('T')[0];
        config.deliveryTime = '10:00 AM';
        break;
      case 'COMPLAINT_RESOLUTION':
        config.complaintCategory = 'general';
        config.escalationEnabled = true;
        break;
      case 'EMERGENCY_RESPONSE':
        config.emergencyType = 'gas_leak';
        config.priority = 'critical';
        break;
      case 'SATISFACTION_SURVEY':
        config.surveyQuestions = [
          'How satisfied are you with our service?',
          'Any suggestions for improvement?'
        ];
        break;
      case 'CUSTOM':
        config.customPurpose = '';
        break;
    }
    
    return config;
  };

  const handleConfigChange = (field, value) => {
    setCampaignConfig(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!selectedCallType) {
      newErrors.callType = 'Please select a call type';
    }
    
    if (!campaignName.trim()) {
      newErrors.campaignName = 'Campaign name is required';
    }
    
    if (!csvFile) {
      newErrors.csvFile = 'Please upload a customer list';
    }
    
    if (selectedCallType === 'CUSTOM' && !campaignConfig.customPurpose?.trim()) {
      newErrors.customPurpose = 'Custom purpose is required for custom campaigns';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    try {
      const campaignData = {
        name: campaignName,
        description: campaignDescription,
        campaignConfig,
      };

      let result;
      if (selectedCallType === 'CUSTOM') {
        result = await createCampaign(campaignData, csvFile, 'custom', campaignConfig.customPurpose);
      } else {
        result = await createCampaign(campaignData, csvFile, selectedCallType, selectedTemplate || null);
      }

      if (result) {
        navigate(`/campaigns/${result.id}`);
      }
    } catch (error) {
      console.error('Failed to create campaign:', error);
    }
  };

  const filteredTemplates = templates?.filter(template => 
    template.callType === selectedCallType || template.publicTemplate
  ) || [];

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Create New Campaign</h1>
          <p className="text-muted-foreground mt-2">
            Choose a campaign type and configure your automated calling campaign
          </p>
        </div>
        <Button variant="outline" onClick={() => navigate('/campaigns')}>
          Cancel
        </Button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Call Type Selection */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Settings className="h-5 w-5" />
              Campaign Type
            </CardTitle>
            <CardDescription>
              Select the type of campaign you want to create
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {CALL_TYPES.map((type) => {
                const config = CALL_TYPE_CONFIGS[type.value];
                return (
                  <div
                    key={type.value}
                    className={`relative cursor-pointer rounded-lg border p-4 transition-colors hover:bg-muted/50 ${
                      selectedCallType === type.value
                        ? 'border-primary bg-primary/5'
                        : 'border-border'
                    }`}
                    onClick={() => setSelectedCallType(type.value)}
                  >
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">{config.icon}</span>
                      <div className="flex-1">
                        <h3 className="font-semibold">{type.label}</h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          {type.description}
                        </p>
                      </div>
                      {selectedCallType === type.value && (
                        <div className="absolute top-2 right-2">
                          <div className="h-2 w-2 rounded-full bg-primary"></div>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
            {errors.callType && (
              <p className="text-sm text-destructive mt-2">{errors.callType}</p>
            )}
          </CardContent>
        </Card>

        {/* Campaign Details */}
        {selectedCallType && (
          <>
            <Card>
              <CardHeader>
                <CardTitle>Campaign Details</CardTitle>
                <CardDescription>
                  Configure the basic information for your {callTypeConfig?.label.toLowerCase()} campaign
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="campaignName">Campaign Name</Label>
                  <Input
                    id="campaignName"
                    value={campaignName}
                    onChange={(e) => setCampaignName(e.target.value)}
                    placeholder={callTypeConfig?.defaultName}
                    className="mt-1"
                  />
                  {errors.campaignName && (
                    <p className="text-sm text-destructive mt-1">{errors.campaignName}</p>
                  )}
                </div>

                <div>
                  <Label htmlFor="campaignDescription">Description</Label>
                  <Textarea
                    id="campaignDescription"
                    value={campaignDescription}
                    onChange={(e) => setCampaignDescription(e.target.value)}
                    placeholder={callTypeConfig?.description}
                    className="mt-1"
                    rows={3}
                  />
                </div>
              </CardContent>
            </Card>

            {/* Template Selection */}
            {selectedCallType !== 'CUSTOM' && (
              <Card>
                <CardHeader>
                  <CardTitle>Agent Template</CardTitle>
                  <CardDescription>
                    Choose a template for your AI agent or use the default template
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {templatesLoading ? (
                    <div className="flex items-center justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin" />
                    </div>
                  ) : (
                    <Select value={selectedTemplate} onValueChange={setSelectedTemplate}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a template (optional)" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="">Use Default Template</SelectItem>
                        {filteredTemplates.map((template) => (
                          <SelectItem key={template.id} value={template.id.toString()}>
                            <div className="flex items-center gap-2">
                              <span>{template.templateName}</span>
                              {template.publicTemplate && (
                                <Badge variant="secondary" className="text-xs">Public</Badge>
                              )}
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                </CardContent>
              </Card>
            )}

            {/* Campaign Configuration */}
            <Card>
              <CardHeader>
                <CardTitle>Campaign Configuration</CardTitle>
                <CardDescription>
                  Configure specific settings for your campaign type
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {selectedCallType === 'DSC_COLLECTION' && (
                  <>
                    <div>
                      <Label htmlFor="deliveryDate">Delivery Date</Label>
                      <Input
                        id="deliveryDate"
                        type="date"
                        value={campaignConfig.deliveryDate || ''}
                        onChange={(e) => handleConfigChange('deliveryDate', e.target.value)}
                        className="mt-1"
                      />
                    </div>
                  </>
                )}

                {selectedCallType === 'PAYMENT_REMINDER' && (
                  <>
                    <div>
                      <Label htmlFor="paymentAmount">Payment Amount (₹)</Label>
                      <Input
                        id="paymentAmount"
                        type="number"
                        value={campaignConfig.paymentAmount || ''}
                        onChange={(e) => handleConfigChange('paymentAmount', e.target.value)}
                        placeholder="850"
                        className="mt-1"
                      />
                    </div>
                    <div>
                      <Label htmlFor="dueDate">Due Date</Label>
                      <Input
                        id="dueDate"
                        type="date"
                        value={campaignConfig.dueDate || ''}
                        onChange={(e) => handleConfigChange('dueDate', e.target.value)}
                        className="mt-1"
                      />
                    </div>
                  </>
                )}

                {selectedCallType === 'DELIVERY_CONFIRMATION' && (
                  <>
                    <div>
                      <Label htmlFor="deliveryDate">Delivery Date</Label>
                      <Input
                        id="deliveryDate"
                        type="date"
                        value={campaignConfig.deliveryDate || ''}
                        onChange={(e) => handleConfigChange('deliveryDate', e.target.value)}
                        className="mt-1"
                      />
                    </div>
                    <div>
                      <Label htmlFor="deliveryTime">Delivery Time</Label>
                      <Input
                        id="deliveryTime"
                        value={campaignConfig.deliveryTime || ''}
                        onChange={(e) => handleConfigChange('deliveryTime', e.target.value)}
                        placeholder="10:00 AM"
                        className="mt-1"
                      />
                    </div>
                  </>
                )}

                {selectedCallType === 'COMPLAINT_RESOLUTION' && (
                  <>
                    <div>
                      <Label htmlFor="complaintCategory">Default Complaint Category</Label>
                      <Select
                        value={campaignConfig.complaintCategory || ''}
                        onValueChange={(value) => handleConfigChange('complaintCategory', value)}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select category" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="general">General</SelectItem>
                          <SelectItem value="delivery">Delivery Issue</SelectItem>
                          <SelectItem value="billing">Billing Issue</SelectItem>
                          <SelectItem value="quality">Quality Issue</SelectItem>
                          <SelectItem value="safety">Safety Concern</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </>
                )}

                {selectedCallType === 'EMERGENCY_RESPONSE' && (
                  <>
                    <div>
                      <Label htmlFor="emergencyType">Emergency Type</Label>
                      <Select
                        value={campaignConfig.emergencyType || ''}
                        onValueChange={(value) => handleConfigChange('emergencyType', value)}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Select emergency type" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="gas_leak">Gas Leak</SelectItem>
                          <SelectItem value="fire">Fire</SelectItem>
                          <SelectItem value="explosion">Explosion</SelectItem>
                          <SelectItem value="medical">Medical Emergency</SelectItem>
                          <SelectItem value="other">Other</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </>
                )}

                {selectedCallType === 'SATISFACTION_SURVEY' && (
                  <div>
                    <Label>Survey Questions</Label>
                    <div className="space-y-2 mt-2">
                      {campaignConfig.surveyQuestions?.map((question, index) => (
                        <Input
                          key={index}
                          value={question}
                          onChange={(e) => {
                            const newQuestions = [...campaignConfig.surveyQuestions];
                            newQuestions[index] = e.target.value;
                            handleConfigChange('surveyQuestions', newQuestions);
                          }}
                          placeholder={`Question ${index + 1}`}
                        />
                      ))}
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          const newQuestions = [...(campaignConfig.surveyQuestions || []), ''];
                          handleConfigChange('surveyQuestions', newQuestions);
                        }}
                      >
                        <Plus className="h-4 w-4 mr-2" />
                        Add Question
                      </Button>
                    </div>
                  </div>
                )}

                {selectedCallType === 'CUSTOM' && (
                  <div>
                    <Label htmlFor="customPurpose">Custom Purpose</Label>
                    <Textarea
                      id="customPurpose"
                      value={campaignConfig.customPurpose || ''}
                      onChange={(e) => handleConfigChange('customPurpose', e.target.value)}
                      placeholder="Describe the purpose of your custom campaign..."
                      className="mt-1"
                      rows={3}
                    />
                    {errors.customPurpose && (
                      <p className="text-sm text-destructive mt-1">{errors.customPurpose}</p>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Customer Upload */}
            <Card>
              <CardHeader>
                <CardTitle>Customer List</CardTitle>
                <CardDescription>
                  Upload a CSV file with customer information (name, phone, address)
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="border-2 border-dashed border-border rounded-lg p-6 text-center">
                  <input
                    type="file"
                    accept=".csv"
                    onChange={(e) => setCsvFile(e.target.files[0])}
                    className="hidden"
                    id="csv-upload"
                  />
                  <label htmlFor="csv-upload" className="cursor-pointer">
                    <div className="space-y-2">
                      <div className="mx-auto h-12 w-12 bg-muted rounded-full flex items-center justify-center">
                        <Plus className="h-6 w-6 text-muted-foreground" />
                      </div>
                      <div>
                        <p className="text-sm font-medium">Click to upload CSV file</p>
                        <p className="text-xs text-muted-foreground">
                          or drag and drop
                        </p>
                      </div>
                      {csvFile && (
                        <p className="text-sm text-green-600">
                          Selected: {csvFile.name}
                        </p>
                      )}
                    </div>
                  </label>
                </div>
                {errors.csvFile && (
                  <p className="text-sm text-destructive mt-2">{errors.csvFile}</p>
                )}
              </CardContent>
            </Card>

            {/* Submit */}
            <CardFooter className="flex justify-end gap-4 px-0">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/campaigns')}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Create Campaign
              </Button>
            </CardFooter>
          </>
        )}
      </form>
    </div>
  );
}
