package com.emak.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
    @NotBlank String nom,
    String prenom,
    String entreprise,
    @Email String email,
    String telephone,
    String adresse,
    String ville,
    String codePostal,
    String pays,
    String typeClient,
    String origine,
    Long idUtilisateurResponsable
) {}