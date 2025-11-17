package com.emak.crm.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.EtapeVente;
import com.emak.crm.enums.StatutOpportunite;

@Repository
public interface OpportuniteRepository extends JpaRepository<Opportunite, Long> {
    
    List<Opportunite> findByClientId(Long clientId);

    @Query("SELECT COUNT(o) FROM Opportunite o")
    long countAllOpportunites();

    // ✅ CORRIGÉ : Ajouter @Query manquant
    @Query("SELECT o.etapeVente, COUNT(o) FROM Opportunite o GROUP BY o.etapeVente")
    List<Object[]> opportunitesParEtape();
    
    List<Opportunite> findByEtapeVente(EtapeVente etapeVente);
    
    List<Opportunite> findByUtilisateur(Utilisateur utilisateur);
    
    List<Opportunite> findByDateCreationBetweenAndStatut(LocalDateTime deb, LocalDateTime fin, StatutOpportunite statutOpp);
    List<Opportunite> findByUtilisateurAndDateCreationBetween(Utilisateur utilisateur, LocalDateTime debut, LocalDateTime fin);
    List<Opportunite> findByUtilisateurAndStatutNot(Utilisateur utilisateur, StatutOpportunite statut);
    
    List<Opportunite> findByStatut(StatutOpportunite statut);
    
    List<Opportunite> findByDateCloturePrevueBeforeAndStatutNot(LocalDate date, StatutOpportunite statut);
    
    @Query("SELECT o FROM Opportunite o WHERE o.dateCreation BETWEEN :debut AND :fin")
    List<Opportunite> findByPeriodeCreation(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT SUM(o.montantEstime * o.probabilite / 100.0) FROM Opportunite o WHERE o.statut = 'EN_COURS'")
    BigDecimal calculateCABrut();
    
    long countByEtapeVenteAndStatut(EtapeVente etapeVente, StatutOpportunite statut);
    
    @Query("SELECT o.etapeVente, COUNT(o) FROM Opportunite o WHERE o.statut = 'EN_COURS' GROUP BY o.etapeVente")
    List<Object[]> countByEtapeVenteAndStatut();

    @Query("SELECT u.nom, SUM(o.montantEstime) FROM Opportunite o JOIN o.utilisateur u WHERE o.statut = 'GAGNE' AND YEAR(o.dateCreation) = YEAR(CURRENT_DATE) GROUP BY u.nom")
    List<Object[]> findPerformanceCommerciale();

    // ✅ CORRIGÉ : Utiliser une fonction H2 compatible
    @Query("SELECT CONCAT(YEAR(o.dateCreation), '-', MONTH(o.dateCreation)), SUM(o.montantEstime) " +
    	       "FROM Opportunite o " +
    	       "WHERE o.statut = 'GAGNEE' AND o.dateCreation >= :debut " +
    	       "GROUP BY CONCAT(YEAR(o.dateCreation), '-', MONTH(o.dateCreation)) " +
    	       "ORDER BY CONCAT(YEAR(o.dateCreation), '-', MONTH(o.dateCreation))")
    	List<Object[]> findVentesParMois(@Param("debut") LocalDateTime debut);

    @Query("SELECT SUM(o.montantEstime * o.probabilite / 100.0) FROM Opportunite o WHERE o.statut = 'EN_COURS'")
    BigDecimal calculateValeurPipeline();

    long countByDateCloturePrevueBeforeAndStatut(LocalDate date, StatutOpportunite statut);

    List<Opportunite> findTop10ByDateModificationAfterOrderByDateModificationDesc(LocalDateTime dateLimite);
    
    long countByStatut(StatutOpportunite statut);
    
    @Query("SELECT COUNT(o) FROM Opportunite o WHERE o.statut IN :statuts")
    long countByStatuts(@Param("statuts") List<StatutOpportunite> statuts);
}