package com.emak.crm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Interaction;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    
    // ✅ Méthodes VALIDES (utilisent des propriétés existantes)
    List<Interaction> findByClientIdOrderByDateInteractionDesc(Long clientId);
    List<Interaction> findByUtilisateurIdOrderByDateInteractionDesc(Long utilisateurId);
    List<Interaction> findByOpportuniteIdOrderByDateInteractionDesc(Long opportuniteId);
    
    // Interactions récentes
    List<Interaction> findTop50ByOrderByDateInteractionDesc();
    List<Interaction> findByDateInteractionAfterOrderByDateInteractionDesc(LocalDateTime date);
    List<Interaction> findTop15ByOrderByDateInteractionDesc();
    
    // Recherche dans les champs existants
    List<Interaction> findByObjetContainingIgnoreCaseOrderByDateInteractionDesc(String terme);
    List<Interaction> findByCompteRenduContainingIgnoreCaseOrderByDateInteractionDesc(String terme);
    
    // Recherche combinée (avec @Query)
    @Query("SELECT i FROM Interaction i WHERE LOWER(i.objet) LIKE LOWER(CONCAT('%', :terme, '%')) OR LOWER(i.compteRendu) LIKE LOWER(CONCAT('%', :terme, '%')) ORDER BY i.dateInteraction DESC")
    List<Interaction> searchInObjetAndCompteRendu(@Param("terme") String terme);
    
    // ✅ AJOUT : Méthode manquante
    List<Interaction> findTop15ByDateInteractionAfterOrderByDateInteractionDesc(LocalDateTime date);
    // Statistiques
    long countByClientId(Long clientId);
    long countByUtilisateurId(Long utilisateurId);
    long countByClientIdAndDateInteractionAfter(Long clientId, LocalDateTime date);
    long countByDateInteractionBetween(LocalDateTime debut, LocalDateTime fin);
    
    // Filtrer par type et résultat
    List<Interaction> findByTypeOrderByDateInteractionDesc(com.emak.crm.enums.TypeInteraction type);
    List<Interaction> findByResultatOrderByDateInteractionDesc(com.emak.crm.enums.ResultatInteraction resultat);
    
 
}