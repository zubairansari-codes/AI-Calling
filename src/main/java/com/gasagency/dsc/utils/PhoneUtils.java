package com.gasagency.dsc.utils;

public class PhoneUtils {

    /**
     * Normalizes a phone number to E.164 format for India (+91) if needed.
     * 1. Removes all non-digit characters.
     * 2. If 10 digits, prepends +91.
     * 3. If 12 digits and starts with 91, prepends +.
     * 4. Otherwise ensures a + is present at the start.
     */
    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) return phone;

        // Remove all non-digit characters
        String digits = phone.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return "+91" + digits;
        } else if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        } else if (digits.length() > 10) {
            return "+" + digits;
        }

        return phone; // Fallback
    }
}
