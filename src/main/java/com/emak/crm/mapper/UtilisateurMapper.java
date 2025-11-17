package com.emak.crm.mapper;

import com.emak.crm.dto.UtilisateurRequest;
import com.emak.crm.dto.UtilisateurResponse;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.Roles;

public class UtilisateurMapper {

    public static Utilisateur toEntity(UtilisateurRequest request) {
        return Utilisateur.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .role(Roles.of(request.role()))
                .equipe(request.equipe())
                .dateEmbauche(request.dateEmbauche() != null ? 
                    java.time.LocalDate.parse(request.dateEmbauche()) : null)
                .actif(true)
                .build();
    }

    public static UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .role(utilisateur.getRole().getNom())
                .equipe(utilisateur.getEquipe())
                .dateEmbauche(utilisateur.getDateEmbauche())
                .actif(utilisateur.getActif())
                .dateCreation(utilisateur.getDateCreation())
                .build();
    }
}