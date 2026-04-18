// Mock data for demo/fallback when backend is unavailable

export const mockDashboard = {
  dscCollectedToday: 47,
  callsMadeToday: 128,
  transferredToday: 12,
  successRateToday: 36.7,
  totalDscAllTime: 2834,
  totalCallsAllTime: 8920,
  totalCampaigns: 45,
  activeCampaign: {
    id: 1, name: 'March 15 Deliveries', deliveryDate: '2026-03-15',
    status: 'IN_PROGRESS', totalCustomers: 250, completedCalls: 178,
    successfulCalls: 89, failedCalls: 34, transferredCalls: 15,
    successRate: 50.0, completionRate: 71.2,
    createdAt: '2026-03-15T08:00:00', startedAt: '2026-03-15T09:00:00', completedAt: null
  },
  recentCalls: [
    { id: 1, customerName: 'Rajesh Kumar', customerPhone: '9876543210', status: 'DSC_COLLECTED', dscNumber: '4582', durationSeconds: 45, attemptCount: 1, calledAt: '2026-03-15T14:30:00', completedAt: '2026-03-15T14:31:00', createdAt: '2026-03-15T08:00:00' },
    { id: 2, customerName: 'Priya Sharma', customerPhone: '9876543211', status: 'TRANSFERRED', dscNumber: null, durationSeconds: 120, attemptCount: 1, transferReason: 'Rate inquiry', calledAt: '2026-03-15T14:25:00', completedAt: '2026-03-15T14:27:00', createdAt: '2026-03-15T08:00:00' },
    { id: 3, customerName: 'Amit Patel', customerPhone: '9876543212', status: 'NO_ANSWER', dscNumber: null, durationSeconds: 0, attemptCount: 2, calledAt: '2026-03-15T14:20:00', completedAt: '2026-03-15T14:20:30', createdAt: '2026-03-15T08:00:00' },
    { id: 4, customerName: 'Sunita Devi', customerPhone: '9876543213', status: 'DSC_COLLECTED', dscNumber: '7291', durationSeconds: 38, attemptCount: 1, calledAt: '2026-03-15T14:15:00', completedAt: '2026-03-15T14:16:00', createdAt: '2026-03-15T08:00:00' },
    { id: 5, customerName: 'Vikram Singh', customerPhone: '9876543214', status: 'IN_PROGRESS', dscNumber: null, durationSeconds: null, attemptCount: 1, calledAt: '2026-03-15T14:32:00', completedAt: null, createdAt: '2026-03-15T08:00:00' },
  ]
};

export const mockCampaigns = [
  { id: 1, name: 'March 15 Deliveries', deliveryDate: '2026-03-15', status: 'IN_PROGRESS', totalCustomers: 250, completedCalls: 178, successfulCalls: 89, failedCalls: 34, transferredCalls: 15, successRate: 50.0, completionRate: 71.2, createdAt: '2026-03-15T08:00:00', startedAt: '2026-03-15T09:00:00' },
  { id: 2, name: 'March 14 Deliveries', deliveryDate: '2026-03-14', status: 'COMPLETED', totalCustomers: 180, completedCalls: 180, successfulCalls: 98, failedCalls: 42, transferredCalls: 20, successRate: 54.4, completionRate: 100, createdAt: '2026-03-14T08:00:00', startedAt: '2026-03-14T09:00:00', completedAt: '2026-03-14T17:00:00' },
  { id: 3, name: 'March 16 Batch', deliveryDate: '2026-03-16', status: 'DRAFT', totalCustomers: 95, completedCalls: 0, successfulCalls: 0, failedCalls: 0, transferredCalls: 0, successRate: 0, completionRate: 0, createdAt: '2026-03-16T08:00:00' },
];

export const mockHourlyData = Array.from({ length: 9 }, (_, i) => ({
  hour: `${9 + i}:00`,
  total: Math.floor(Math.random() * 30) + 5,
  dsc: Math.floor(Math.random() * 15) + 2,
}));

export const mockOutcomeData = [
  { name: 'DSC Collected', value: 89, color: '#10b981' },
  { name: 'Transferred', value: 15, color: '#8b5cf6' },
  { id: 4, name: "Ramesh Sharma", phone: "+91 9765432109", __id__: "cus_4" },
  { id: 5, name: "Sita Verma", phone: "+91 9654321098", __id__: "cus_5" },
];

export const mockAgencies = [
  { id: 1, name: "Sharma Gas Agency", ownerName: "Ramesh Sharma", city: "Delhi", email: "admin@sharmagas.com", phone: "9876543210", plan: "Starter Pro", usageThisMonth: 450, isActive: true, createdAt: "2024-01-15" },
  { id: 2, name: "Verma Enterprises", ownerName: "Vikram Verma", city: "Mumbai", email: "vikram@verma-ent.in", phone: "9123456780", plan: "Basic", usageThisMonth: 210, isActive: true, createdAt: "2024-02-02" },
  { id: 3, name: "Indane South", ownerName: "Priya Rao", city: "Bengaluru", email: "priya@indanesouth.com", phone: "9988776655", plan: "Enterprise", usageThisMonth: 1250, isActive: true, createdAt: "2023-11-20" },
  { id: 4, name: "Singh Distributors", ownerName: "Amar Singh", city: "Chandigarh", email: "amar@singhdist.com", phone: "9811223344", plan: "Starter Pro", usageThisMonth: 0, isActive: false, createdAt: "2024-03-10" }
];
