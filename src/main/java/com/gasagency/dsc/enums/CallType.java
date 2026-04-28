package com.gasagency.dsc.enums;

/**
 * Comprehensive call types for dynamic calling platform
 * Supports any purpose beyond just DSC collection
 */
public enum CallType {
    
    // Original DSC Collection (backward compatibility)
    DSC_COLLECTION("DSC Collection", "Collect delivery service codes from customers"),
    
    // Payment & Billing
    PAYMENT_REMINDER("Payment Reminder", "Remind customers about pending payments"),
    PAYMENT_CONFIRMATION("Payment Confirmation", "Confirm received payments"),
    OVERDUE_FOLLOWUP("Overdue Follow-up", "Follow up on overdue payments"),
    
    // Delivery & Booking
    DELIVERY_CONFIRMATION("Delivery Confirmation", "Confirm successful deliveries"),
    DELIVERY_RESCHEDULING("Delivery Rescheduling", "Reschedule missed deliveries"),
    BOOKING_CONFIRMATION("Booking Confirmation", "Confirm new cylinder bookings"),
    REFILL_REMINDER("Refill Reminder", "Remind customers to book refills"),
    
    // Customer Service
    COMPLAINT_RESOLUTION("Complaint Resolution", "Handle customer complaints"),
    SATISFACTION_SURVEY("Satisfaction Survey", "Conduct customer satisfaction surveys"),
    FEEDBACK_COLLECTION("Feedback Collection", "Collect general feedback"),
    
    // Emergency & Safety
    EMERGENCY_RESPONSE("Emergency Response", "Handle emergency calls like gas leaks"),
    SAFETY_AWARENESS("Safety Awareness", "Educate about gas safety"),
    
    // Marketing & Communication
    NEW_SCHEME_ANNOUNCEMENT("New Scheme Announcement", "Announce new government schemes"),
    PROMOTIONAL_CALL("Promotional Call", "Promote new products or services"),
    GENERAL_ANNOUNCEMENT("General Announcement", "Make general announcements"),
    
    // Custom/User Defined
    CUSTOM("Custom", "User-defined custom call purpose");
    
    private final String displayName;
    private final String description;
    
    CallType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this is a built-in system call type
     */
    public boolean isSystemType() {
        return this != CUSTOM;
    }
    
    /**
     * Check if this call type requires urgent handling
     */
    public boolean isUrgent() {
        return this == EMERGENCY_RESPONSE || this == COMPLAINT_RESOLUTION;
    }
    
    /**
     * Check if this call type involves financial transactions
     */
    public boolean isFinancial() {
        return this == PAYMENT_REMINDER || 
               this == PAYMENT_CONFIRMATION || 
               this == OVERDUE_FOLLOWUP;
    }
    
    /**
     * Check if this call type is for data collection
     */
    public boolean isDataCollection() {
        return this == DSC_COLLECTION || 
               this == SATISFACTION_SURVEY || 
               this == FEEDBACK_COLLECTION;
    }
}
