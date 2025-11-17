package com.emak.crm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

/**
 * @author emmanuel kouadio
 * Response DTO 
 * {@link com.emak.crm.entity.Utilisateur}
 * 
 */

@Builder
public record UtilisateurResponse(
	    Long id,
	    String nom,
	    String prenom,
	    String email,
	    String telephone,
	    String role,
	    String equipe,
	    LocalDate dateEmbauche,
	    Boolean actif,
	    LocalDateTime dateCreation
) {}
