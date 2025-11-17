package com.emak.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurMinimalDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
}
