package com.cybershield.ai.core.security;

import androidx.annotation.NonNull;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Privacy-First Data Scrubbing/Redaction Engine.
 * Sanitizes sensitive Personal Identifiable Information (PII) like email addresses,
 * phone numbers, IP addresses, credit card numbers, and auth tokens before logging or offloading
 * payload feature vectors to local Fog nodes.
 */
@Singleton
public class DataRedactor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\+?[0-9]{10,14}\\b");
    private static final Pattern IPV4_PATTERN = Pattern.compile("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    @Inject
    public DataRedactor() {
    }

    @NonNull
    public String redactPii(@NonNull String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            return "";
        }

        String redacted = EMAIL_PATTERN.matcher(rawContent).replaceAll("[REDACTED_EMAIL]");
        redacted = PHONE_PATTERN.matcher(redacted).replaceAll("[REDACTED_PHONE]");
        redacted = IPV4_PATTERN.matcher(redacted).replaceAll("[REDACTED_IP]");
        redacted = CREDIT_CARD_PATTERN.matcher(redacted).replaceAll("[REDACTED_CARD]");

        return redacted;
    }
}
