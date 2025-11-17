package com.emak.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Campagne;
import com.emak.crm.enums.StatutCampagne;

@Repository
public interface CampagneRepository extends JpaRepository<Campagne, Long>{
	@Query("SELECT c FROM Campagne c WHERE c.statut IN :statuts")
	List<Campagne> getCampagnesActives(@Param("statuts") List<StatutCampagne> statuts);
	
	List<Campagne> findByStatutIn(List<StatutCampagne> statuts);
}
