package com.emak.crm.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ClientResponse(
    Long id,
    String nom,
    String prenom,
    String entreprise,
    String email,
    String telephone,
    String adresse,
    String ville,
    String codePostal,
    String pays,
    String typeClient,
    String statut,
    Integer scoreProspect,
    String origine,
    Long idUtilisateurResponsable,
    String nomUtilisateurResponsable,
    String fonction,
    String siteWeb,
    String secteurActivite,
    String notes,
    LocalDateTime dateCreation,
    LocalDateTime dateModification
) {}