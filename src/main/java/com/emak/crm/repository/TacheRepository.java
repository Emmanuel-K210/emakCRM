package com.emak.crm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emak.crm.entity.Client;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Tache;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutTache;


public interface TacheRepository extends JpaRepository<Tache, Long> {
	List<Tache> findByUtilisateur(Utilisateur utilisateur);
    
    List<Tache> findByOpportunite(Opportunite opportunite);
    
    List<Tache> findByDateEcheanceBetween(LocalDateTime start, LocalDateTime end);
    
    List<Tache> findByDateEcheanceBeforeAndStatutNot(LocalDateTime date, StatutTache statut);
    
    List<Tache> findByStatut(StatutTache statut);
    
    List<Tache> findByClient(Client client);
}
