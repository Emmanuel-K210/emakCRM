package com.emak.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.NoteInterne;
import com.emak.crm.entity.Utilisateur;

@Repository
public interface NoteInterneRepository extends JpaRepository<NoteInterne, Long> {

	  @Query(value = "SELECT * FROM note_interne n WHERE " +
              "LOWER(n.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
              "LOWER(n.contenu) LIKE LOWER(CONCAT('%', :search, '%')) " +
              "ORDER BY n.date_creation DESC", 
      nativeQuery = true)
List<NoteInterne> searchByTitreOrContenu(@Param("search") String search);
    
    // ✅ Méthodes dérivées automatiques (utilisent les noms exacts des propriétés)
    List<NoteInterne> findByAuteur(Utilisateur auteur);
    List<NoteInterne> findByAuteurOrderByDateCreationDesc(Utilisateur auteur);
    List<NoteInterne> findByPrivee(Boolean privee);
    List<NoteInterne> findByPriveeOrderByDateCreationDesc(Boolean privee);
    List<NoteInterne> findByPriveeAndAuteur(Boolean privee, Utilisateur auteur);
    
    // ✅ Méthodes pour client et opportunité
    List<NoteInterne> findByClientId(Long clientId);
    List<NoteInterne> findByOpportuniteId(Long opportuniteId);
    List<NoteInterne> findByClientIdOrderByDateCreationDesc(Long clientId);
    List<NoteInterne> findByOpportuniteIdOrderByDateCreationDesc(Long opportuniteId);
    
    // ✅ Méthodes utilitaires
    List<NoteInterne> findTop10ByOrderByDateCreationDesc();
    
    
  /*  // ✅ CORRIGÉ : Méthode pour trouver les notes publiques OU celles de l'auteur
    @Query("SELECT n FROM NoteInterne n WHERE n.privee = false OR n.auteur = :auteur ORDER BY n.dateCreation DESC")
    List<NoteInterne> findByPubliqueOrAuteur(@Param("auteur") Utilisateur auteur);
    
    // ✅ Si vous voulez la méthode originale à 2 paramètres
    @Query("SELECT n FROM NoteInterne n WHERE (LOWER(n.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.contenu) LIKE LOWER(CONCAT('%', :search, '%'))) AND n.auteur = :auteur")
    List<NoteInterne> searchByTitreOrContenuAndAuteur(@Param("search") String search, @Param("auteur") Utilisateur auteur);*/
}