package com.emak.crm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Devis;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {

	 // Pour la génération du numéro
    @Query("SELECT COUNT(d) FROM Devis d WHERE YEAR(d.dateCreation) = :annee")
    Long countByDateCreationYear(@Param("annee") int annee);

	// Chargement eager des relations fréquentes
	@Query("SELECT d FROM Devis d LEFT JOIN FETCH d.opportunite o LEFT JOIN FETCH o.client WHERE d.id = :id")
	Optional<Devis> findByIdWithOpportuniteAndClient(@Param("id") Long id);

	// Pour les relances : devis en attente et bientôt expirés
	@Query("SELECT d FROM Devis d WHERE d.statut = 'EN_ATTENTE' AND d.dateValidite BETWEEN :today AND :dateLimite")
	List<Devis> findDevisARelancer(@Param("today") LocalDate today, @Param("dateLimite") LocalDate dateLimite);

	// Pour conversion facture : devis acceptés et non encore facturés
	@Query("SELECT d FROM Devis d WHERE d.statut = 'ACCEPTE' AND d.dejaFacture = false AND d.dateValidite >= :today")
	List<Devis> findDevisConvertiblesEnFacture(@Param("today") LocalDate today);
	
	 // Pour trouver les devis envoyés mais pas encore acceptés/refusés
    @Query("SELECT d FROM Devis d WHERE d.statut = 'ENVOYE' AND d.dateValidite >= :today")
    List<Devis> findDevisEnvoyesNonExpires(@Param("today") LocalDate today);
    
    
    
}
