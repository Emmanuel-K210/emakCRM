package com.emak.crm.event;

import java.time.LocalDateTime;

public abstract class TacheEvent {
    private final Long tacheId;
    private final String titre;
    private final LocalDateTime timestamp;

    public TacheEvent(Long tacheId, String titre) {
        this.tacheId = tacheId;
        this.titre = titre;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public Long getTacheId() { return tacheId; }
    public String getTitre() { return titre; }
    public LocalDateTime getTimestamp() { return timestamp; }
}