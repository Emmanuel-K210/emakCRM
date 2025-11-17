package com.emak.crm.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.emak.crm.dto.CampagneRequest;
import com.emak.crm.dto.CampagneResponse;
import com.emak.crm.dto.EnvoiMinimalDTO;
import com.emak.crm.dto.ListeDiffusionMinimalDTO;
import com.emak.crm.dto.UtilisateurMinimalDTO;
import com.emak.crm.entity.Campagne;
import com.emak.crm.entity.Envoi;
import com.emak.crm.entity.ListeDiffusion;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutCampagne;

public class CampagneMapper {
    
    public static Campagne toEntity(CampagneRequest dto) {
        return Campagne.builder()
                .nomCampagne(dto.getNomCampagne())
                .type(dto.getType())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .budget(dto.getBudget())
                .objectif(dto.getObjectif())
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutCampagne.PLANIFIEE)
                .tauxConversion(dto.getTauxConversion())
                .utilisateurResponsable(Utilisateur.builder().id(dto.getUtilisateurResponsableId()).build())
                .build();
    }
    
    public static CampagneResponse toResponseDTO(Campagne campagne) {
    	
        return CampagneResponse.builder()
                .id(campagne.getId())
                .nomCampagne(campagne.getNomCampagne())
                .type(campagne.getType())
                .dateDebut(campagne.getDateDebut())
                .dateFin(campagne.getDateFin())
                .budget(campagne.getBudget())
                .objectif(campagne.getObjectif())
                .statut(campagne.getStatut())
                .tauxConversion(campagne.getTauxConversion())
                .utilisateurResponsable(mapUtilisateurToMinimalDTO(campagne.getUtilisateurResponsable()))
                .listes(mapListesToMinimalDTOs(campagne.getListes()))
                .envois(mapEnvoisToMinimalDTOs(campagne.getEnvois()))
                .dateCreation(campagne.getDateCreation())
                .nombreTotalEnvois(calculerNombreTotalEnvois(campagne))
                .nombreContactsTotal(calculerNombreContactsTotal(campagne))
                .build();
    }
    
    private static UtilisateurMinimalDTO mapUtilisateurToMinimalDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        
        return UtilisateurMinimalDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .build();
    }
    
    private static List<ListeDiffusionMinimalDTO> mapListesToMinimalDTOs(List<ListeDiffusion> listes) {
        if (listes == null) return List.of();
        
        return listes.stream()
                .map(CampagneMapper::mapListeToMinimalDTO)
                .collect(Collectors.toList());
    }
    
    private static ListeDiffusionMinimalDTO mapListeToMinimalDTO(ListeDiffusion liste) {
        return ListeDiffusionMinimalDTO.builder()
                .id(liste.getId())
                .nomListe(liste.getNomListe())
                .description(liste.getDescription())
                .nombreContacts(liste.getNombreContacts())
                .build();
    }
    
    private static List<EnvoiMinimalDTO> mapEnvoisToMinimalDTOs(List<Envoi> envois) {
        if (envois == null) return List.of();
        
        return envois.stream()
                .map(CampagneMapper::mapEnvoiToMinimalDTO)
                .collect(Collectors.toList());
    }
    
    private static EnvoiMinimalDTO mapEnvoiToMinimalDTO(Envoi envoi) {
        return EnvoiMinimalDTO.builder()
                .id(envoi.getId())
                .objet(envoi.getObjet())
                .dateEnvoi(envoi.getDateEnvoi())
                .statut(envoi.getStatut())
                .build();
    }
    
    private static Long calculerNombreTotalEnvois(Campagne campagne) {
        return campagne.getEnvois() != null ? (long) campagne.getEnvois().size() : 0L;
    }
    
    private static Long calculerNombreContactsTotal(Campagne campagne) {
        if (campagne.getListes() == null) return 0L;
        
        return (long)campagne.getListes().size();
    }
    
    public static void updateEntityFromDTO(CampagneRequest dto, Campagne campagne) {
        if (dto.getNomCampagne() != null) {
            campagne.setNomCampagne(dto.getNomCampagne());
        }
        if (dto.getType() != null) {
            campagne.setType(dto.getType());
        }
        if (dto.getDateDebut() != null) {
            campagne.setDateDebut(dto.getDateDebut());
        }
        if (dto.getDateFin() != null) {
            campagne.setDateFin(dto.getDateFin());
        }
        if (dto.getBudget() != null) {
            campagne.setBudget(dto.getBudget());
        }
        if (dto.getObjectif() != null) {
            campagne.setObjectif(dto.getObjectif());
        }
        if (dto.getStatut() != null) {
            campagne.setStatut(dto.getStatut());
        }
        if (dto.getTauxConversion() != null) {
            campagne.setTauxConversion(dto.getTauxConversion());
        }
    }
}