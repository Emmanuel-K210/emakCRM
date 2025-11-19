package com.emak.crm.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.TacheRequest;
import com.emak.crm.dto.TacheResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.TacheService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/taches")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class TacheApiController {
    
    private final TacheService tacheService;

    @GetMapping
    public List<TacheResponse> getAllTaches(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long opportuniteId) {
        
        if (statut != null) {
            return tacheService.getTachesByStatut(statut);
        }
        if (utilisateurId != null) {
            try {
				return tacheService.getTachesByUtilisateur(utilisateurId);
			} catch (EntityNotFound e) {
				return List.of();
			}
        }
        
        if (clientId != null) {
            // Implémenter cette méthode dans le service
            // return tacheService.getTachesByClient(clientId);
        }
        
        if (opportuniteId != null) {
            try {
				return tacheService.getTachesByOpportunite(opportuniteId);
			} catch (EntityNotFound e) {
				return List.of();
			}
        }
        
        // Pour l'instant retourner vide, à implémenter
        return List.of();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TacheResponse> getTacheById(@PathVariable Long id) {
        try {
            TacheResponse tache = tacheService.getTacheById(id);
            return ResponseEntity.ok(tache);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTache(@RequestBody TacheRequest request) {
        try {
            TacheResponse nouvelleTache = tacheService.createTache(request);
            return ResponseEntity.ok(nouvelleTache);
        } catch (EntityNotFound e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur création tâche: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTache(@PathVariable Long id, @RequestBody TacheRequest request) {
        try {
            TacheResponse tacheMaj = tacheService.updateTache(id, request);
            return ResponseEntity.ok(tacheMaj);
        } catch (EntityNotFound e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur mise à jour: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestBody Map<String, String> update) {
        try {
            String nouveauStatut = update.get("statut");
            TacheResponse tache = tacheService.updateStatutTache(id, nouveauStatut);
            return ResponseEntity.ok(tache);
        } catch (EntityNotFound e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur mise à jour statut: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTache(@PathVariable Long id) {
        try {
            tacheService.deleteTache(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFound e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Erreur suppression: " + e.getMessage()));
        }
    }
}