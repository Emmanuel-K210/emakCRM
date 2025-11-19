package com.emak.crm.controller;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.emak.crm.event.TacheCreatedEvent;
import com.emak.crm.event.TacheDeletedEvent;
import com.emak.crm.event.TacheUpdatedEvent;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TacheNotificationController {
    
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleTacheCreated(TacheCreatedEvent event) {
        Map<String, Object> notification = Map.of(
            "type", "TACHE_CREEE",
            "tacheId", event.getTacheId(),
            "titre", event.getTitre(),
            "message", "Nouvelle tâche créée: " + event.getTitre(),
            "timestamp", event.getTimestamp()
        );
        messagingTemplate.convertAndSend("/topic/taches", notification);
    }

    @EventListener
    public void handleTacheUpdated(TacheUpdatedEvent event) {
        Map<String, Object> notification = Map.of(
            "type", "TACHE_MODIFIEE",
            "tacheId", event.getTacheId(),
            "titre", event.getTitre(),
            "message", "Tâche modifiée: " + event.getTitre(),
            "timestamp", event.getTimestamp()
        );
        messagingTemplate.convertAndSend("/topic/taches", notification);
    }

    @EventListener
    public void handleTacheDeleted(TacheDeletedEvent event) {
        Map<String, Object> notification = Map.of(
            "type", "TACHE_SUPPRIMEE",
            "tacheId", event.getTacheId(),
            "titre", event.getTitre(),
            "message", "Tâche supprimée: " + event.getTitre(),
            "timestamp", event.getTimestamp()
        );
        messagingTemplate.convertAndSend("/topic/taches", notification);
    }
}