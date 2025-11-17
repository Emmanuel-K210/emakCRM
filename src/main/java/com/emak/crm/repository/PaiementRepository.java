package com.emak.crm.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Paiement;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

	@Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.facture.id = :factureId")
	BigDecimal findMontantTotalPayeByFacture(@Param("factureId") Long factureId);

	List<Paiement> findByFactureIdOrderByDatePaiementDesc(Long factureId);

}
