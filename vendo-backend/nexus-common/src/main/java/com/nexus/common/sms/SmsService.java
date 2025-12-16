package com.nexus.common.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    
    @Value("${app.sms.provider:twilio}")
    private String smsProvider;
    
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;
    
    /**
     * Send SMS message
     */
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.warn("SMS service is disabled. Message would be sent to: {}", phoneNumber);
            return;
        }
        
        try {
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    sendViaTwilio(phoneNumber, message);
                    break;
                case "aws-sns":
                    sendViaAwsSns(phoneNumber, message);
                    break;
                default:
                    log.error("Unknown SMS provider: {}", smsProvider);
            }
            log.info("SMS sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
    
    /**
     * Send OTP via SMS
     */
    public void sendOtp(String phoneNumber, String otp) {
        String message = "Your OTP code is: " + otp + ". Valid for 5 minutes.";
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send verification code via SMS
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        String message = "Your verification code is: " + code + ". Valid for 10 minutes.";
        sendSms(phoneNumber, message);
    }
    
    /**
     * Send via Twilio
     */
    private void sendViaTwilio(String phoneNumber, String message) {
        // TODO: Implement Twilio integration
        // TwilioRestClient client = new TwilioRestClient.Builder(accountSid, authToken).build();
        log.info("Sending SMS via Twilio to: {}", phoneNumber);
    }
    
    /**
     * Send via AWS SNS
     */
    private void sendViaAwsSns(String phoneNumber, String message) {
        // TODO: Implement AWS SNS integration
        // AmazonSNSClient snsClient = new AmazonSNSClient();
        log.info("Sending SMS via AWS SNS to: {}", phoneNumber);
    }
}
