package com.emak.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.emak.crm.enums.StatutCampagne;
import com.emak.crm.enums.TypeCampagne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampagneResponse {
    
    private Long id;
    
    private String nomCampagne;
    
    private TypeCampagne type;
    
    private LocalDate dateDebut;
    
    private LocalDate dateFin;
    
    private BigDecimal budget;
    
    private String objectif;
    
    private StatutCampagne statut;
    
    private BigDecimal tauxConversion;
    
    private UtilisateurMinimalDTO utilisateurResponsable;
    
    private List<ListeDiffusionMinimalDTO> listes;
    
    private List<EnvoiMinimalDTO> envois;
    
    private LocalDateTime dateCreation;
    
    // Métriques calculées (optionnel)
    private Long nombreTotalEnvois;
    private Long nombreContactsTotal;
    private BigDecimal tauxOuverture;
    private BigDecimal tauxClic;
}