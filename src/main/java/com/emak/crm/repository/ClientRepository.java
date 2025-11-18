package com.emak.crm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Client;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>,JpaSpecificationExecutor<Client>{
	 @Query("SELECT COUNT(c) FROM Client c")
	 long countAllClients();
	 
	    /**
	     * RECHERCHE SIMPLE SUR MULTIPLES CHAMPS
	     * Métier : Recherche insensible à la casse sur les principaux champs
	     */
	    @Query("SELECT c FROM Client c WHERE " +
	           "LOWER(c.nom) LIKE LOWER(:searchTerm) OR " +
	           "LOWER(c.prenom) LIKE LOWER(:searchTerm) OR " +
	           "LOWER(c.entreprise) LIKE LOWER(:searchTerm) OR " +
	           "LOWER(c.email) LIKE LOWER(:searchTerm) OR " +
	           "LOWER(c.ville) LIKE LOWER(:searchTerm) OR " +
	           "LOWER(c.telephone) LIKE LOWER(:searchTerm)")
	    List<Client> findBySearchTerm(@Param("searchTerm") String searchTerm);
	    
	    /**
	     * RECHERCHE PAR ENTREPRISE
	     * Métier : Trouver clients par nom d'entreprise (exact ou partiel)
	     */
	    List<Client> findByEntrepriseContainingIgnoreCase(String entreprise);
	    
	    /**
	     * RECHERCHE PAR TYPE ET STATUT
	     * Métier : Combinaison type client + statut pour segmentation
	     */
	    List<Client> findByTypeClientAndStatut(TypeClient typeClient, StatutClient statut);
	    
	    
	    /**
	     * RECHERCHE PAR SCORE
	     * Métier : Clients avec score dans une plage spécifique
	     */
	    List<Client> findByScoreProspectBetween(Integer minScore, Integer maxScore);
	    
	    
	    /**
	     * RECHERCHE PAR COMMERCIAL RESPONSABLE
	     * Métier : Portefeuille clients d'un commercial spécifique
	     */
	    List<Client> findByUtilisateurResponsableId(Long utilisateurId);
	    
	    
	    /**
	     * SUGGESTIONS AUTO-COMPLÉTION
	     * Métier : Suggestions pour la recherche en temps réel
	     */
	    @Query("SELECT c.entreprise FROM Client c WHERE " +
	           "LOWER(c.entreprise) LIKE LOWER(:term) AND c.entreprise IS NOT NULL " +
	           "UNION " +
	           "SELECT CONCAT(c.prenom, ' ', c.nom) FROM Client c WHERE " +
	           "(LOWER(c.prenom) LIKE LOWER(:term) OR LOWER(c.nom) LIKE LOWER(:term)) " +
	           "AND c.prenom IS NOT NULL AND c.nom IS NOT NULL")
	    List<String> findSuggestions(@Param("term") String term);
	    
	 // ClientRepository
	    Integer countByUtilisateurResponsableAndDateCreationBetween(
	            Utilisateur utilisateurResponsable, 
	            LocalDateTime startDate, 
	            LocalDateTime endDate
	        );
	    long countByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);

		Long countByDateCreationAfter(LocalDateTime minusDays);

		Long countByStatut(StatutClient actif);
}
