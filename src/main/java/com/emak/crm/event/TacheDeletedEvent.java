package com.emak.crm.event;

public class TacheDeletedEvent extends TacheEvent {
    public TacheDeletedEvent(Long tacheId, String titre) {
        super(tacheId, titre);
    }
}