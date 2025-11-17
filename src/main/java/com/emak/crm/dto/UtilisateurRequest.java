package com.emak.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UtilisateurRequest(@NotBlank String nom, @NotBlank String prenom, @NotBlank @Email String email,
		String telephone, @NotBlank String role, String equipe, String dateEmbauche,String motPasse){
}
