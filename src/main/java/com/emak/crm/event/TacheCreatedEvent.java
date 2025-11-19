package com.emak.crm.event;

public class TacheCreatedEvent extends TacheEvent {
    public TacheCreatedEvent(Long tacheId, String titre) {
        super(tacheId, titre);
    }
}