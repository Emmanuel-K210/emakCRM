package com.emak.crm.service.impl;


import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    @Value("${app.email.sender}")
    private String emailSender;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    public void envoyerEmail(String destinataire, String objet, String contenu) {
        if (!emailEnabled) {
            log.info("📧 [SIMULATION] Email envoyé à {} - Objet: {}", destinataire, objet);
            log.debug("Contenu: {}", contenu);
            return;
        }
        
        try {
            // Implémentation réelle avec JavaMail ou Spring Mail
            envoyerEmailReel(destinataire, objet, contenu);
            log.info("✅ Email envoyé avec succès à {}", destinataire);
            
        } catch (Exception e) {
            log.error("❌ Erreur envoi email à {}: {}", destinataire, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
    
    private void envoyerEmailReel(String destinataire, String objet, String contenu) {
        // Implémentation avec JavaMailSender (Spring Boot)
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSender);
        message.setTo(destinataire);
        message.setSubject(objet);
        message.setText(contenu);
        javaMailSender.send(message);
        */
        
        // Pour l'instant, simulation
        log.info("📧 ENVOI RÉEL - De: {}, À: {}, Objet: {}", emailSender, destinataire, objet);
    }
    
    public void envoyerEmailAvecTemplate(String destinataire, String objet, String templateName, Map<String, Object> variables) {
        String contenu = genererContenuFromTemplate(templateName, variables);
        envoyerEmail(destinataire, objet, contenu);
    }
    
    private String genererContenuFromTemplate(String templateName, Map<String, Object> variables) {
        // Implémentation avec Thymeleaf ou FreeMarker
        return "Contenu généré depuis template: " + templateName;
    }
}