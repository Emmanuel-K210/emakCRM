package com.emak.crm.dto;

import com.emak.crm.entity.NoteInterne;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.TypeNote;

public class NoteInterneMapper {

    public static NoteInterne toEntity(NoteInterneRequest request, Utilisateur auteur) {
        return NoteInterne.builder()
                .titre(request.titre())
                .contenu(request.contenu())
                .type(TypeNote.valueOf(request.type()))
                .privee(request.privee() != null ? request.privee() : false)
                .auteur(auteur)
                .build();
    }

    public static NoteInterneResponse toResponse(NoteInterne note) {
        return new NoteInterneResponse(
            note.getId(),
            note.getTitre(),
            note.getContenu(),
            note.getType().name(),
            note.getPrivee(),
            note.getAuteur().getId(),
            note.getAuteur().getNom(),
            note.getAuteur().getPrenom(),
            note.getClient() != null ? note.getClient().getId() : null,
            note.getClient() != null ? 
                (note.getClient().getEntreprise() != null ? 
                    note.getClient().getEntreprise() : 
                    note.getClient().getPrenom() + " " + note.getClient().getNom()) : null,
            note.getClient() != null ? note.getClient().getEntreprise() : null,
            note.getOpportunite() != null ? note.getOpportunite().getId() : null,
            note.getOpportunite() != null ? note.getOpportunite().getNomOpportunite() : null,
            note.getDateCreation(),
            note.getDateModification()
        );
    }
    
    public static void updateEntityFromRequest(NoteInterne note, NoteInterneRequest request) {
        if (request.titre() != null) {
            note.setTitre(request.titre());
        }
        if (request.contenu() != null) {
            note.setContenu(request.contenu());
        }
        if (request.type() != null) {
            note.setType(TypeNote.valueOf(request.type()));
        }
        if (request.privee() != null) {
            note.setPrivee(request.privee());
        }
    }
}