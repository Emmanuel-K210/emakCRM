package com.emak.crm.event;

public class TacheUpdatedEvent extends TacheEvent {
    public TacheUpdatedEvent(Long tacheId, String titre) {
        super(tacheId, titre);
    }
}