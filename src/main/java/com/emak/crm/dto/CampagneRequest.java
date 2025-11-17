package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.emak.crm.enums.StatutCampagne;
import com.emak.crm.enums.TypeCampagne;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampagneRequest {
    
    @NotBlank(message = "Le nom de la campagne est obligatoire")
    @Size(max = 255, message = "Le nom de la campagne ne peut pas dépasser 255 caractères")
    private String nomCampagne;
    
    @NotNull(message = "Le type de campagne est obligatoire")
    private TypeCampagne type;
    
    private LocalDate dateDebut;
    
    private LocalDate dateFin;
    
    private BigDecimal budget;
    
    @Size(max = 500, message = "L'objectif ne peut pas dépasser 500 caractères")
    private String objectif;
    
    private StatutCampagne statut;
    
    @NotNull(message = "L'ID de l'utilisateur responsable est obligatoire")
    private Long utilisateurResponsableId;
    
    private BigDecimal tauxConversion;
    
}