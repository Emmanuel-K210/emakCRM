package com.emak.crm.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SMSService {
    
    @Value("${app.sms.sender}")
    private String smsSender;
    
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;
    
    public void envoyerSMS(String telephone, String message) {
        if (!smsEnabled) {
            log.info("📱 [SIMULATION] SMS envoyé à {} - Message: {}", telephone, message);
            return;
        }
        
        try {
            // Implémentation réelle avec API SMS (Twilio, etc.)
            envoyerSMSReel(telephone, message);
            log.info("✅ SMS envoyé avec succès à {}", telephone);
            
        } catch (Exception e) {
            log.error("❌ Erreur envoi SMS à {}: {}", telephone, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du SMS", e);
        }
    }
    
    private void envoyerSMSReel(String telephone, String message) {
        // Implémentation avec Twilio
        /*
        Twilio.init(accountSid, authToken);
        Message.creator(
            new PhoneNumber(telephone),
            new PhoneNumber(smsSender),
            message
        ).create();
        */
        
        // Pour l'instant, simulation
        log.info("📱 ENVOI RÉEL - De: {}, À: {}, Message: {}", smsSender, telephone, message);
    }
    
    public void envoyerSMSGroup(List<String> telephones, String message) {
        telephones.forEach(tel -> envoyerSMS(tel, message));
    }
}