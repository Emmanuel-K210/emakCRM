package com.emak.crm.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Facture;
import com.emak.crm.enums.StatutFacture;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    
    // Méthodes de comptage
    long countByStatut(StatutFacture statut);
    
    @Query("SELECT COUNT(f) FROM Facture f WHERE f.statut = 'NON_PAYEE'")
    long countNonPayees();
    
    // Méthodes de somme
    @Query("SELECT SUM(f.montantTtc) FROM Facture f")
    BigDecimal totalMontantTtc();
    
    @Query("SELECT SUM(f.montantTtc) FROM Facture f WHERE " +
            "f.dateFacture BETWEEN :startDate AND :endDate AND " +
            "f.statut = :statut")
     BigDecimal sumMontantTtcByDateFactureBetweenAndStatut(
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate,
         @Param("statut") StatutFacture statut);
    
    @Query("SELECT SUM(f.montantTtc) FROM Facture f WHERE f.statut = :statut")
    BigDecimal sumMontantTtcByStatut(@Param("statut") StatutFacture statut);
    
    // Recherches
    List<Facture> findByClientId(Long clientId);
    List<Facture> findByStatut(StatutFacture statut);
    List<Facture> findByDateFactureBetween(LocalDate start, LocalDate end);
    List<Facture> findByStatutAndDateEcheanceBefore(StatutFacture statut, LocalDate date);
    @Query("SELECT COUNT(f) FROM Facture f WHERE YEAR(f.dateFacture) = :annee")
    Long countByDateFacturationYear(@Param("annee") int annee);
    
    
    // ❌ CORRIGÉ : Supprimer les références à opportunité qui n'existe pas
    @Query("SELECT f FROM Facture f LEFT JOIN FETCH f.client WHERE f.devis.id = :devisId")
    List<Facture> findByDevisIdWithDetails(@Param("devisId") Long devisId);
    
    // Recherches avec jointures
    @Query("SELECT f FROM Facture f LEFT JOIN FETCH f.client WHERE f.id = :id")
    Optional<Facture> findByIdWithClient(@Param("id") Long id);
    // ❌ CORRIGÉ : Supprimer la référence à opportunité
    @Query("SELECT f FROM Facture f LEFT JOIN FETCH f.client WHERE f.id = :id")
    Optional<Facture> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT f FROM Facture f LEFT JOIN FETCH f.client WHERE f.devis.id = :devisId")
    List<Facture> findByDevisIdWithClient(@Param("devisId") Long devisId);
}