package com.emak.crm.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
@CrossOrigin(origins = "http://localhost:8081")
public class TacheApiController {
    
    private final TacheService tacheService;

    @GetMapping
    public ResponseEntity<?> getAllTaches(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long opportuniteId) {
        
        try {
            List<TacheResponse> taches;
            
            if (statut != null) {
                taches = tacheService.getTachesByStatut(statut);
            } else if (utilisateurId != null) {
                taches = tacheService.getTachesByUtilisateur(utilisateurId);
            } else if (clientId != null) {
                taches = tacheService.getTachesByClient(clientId);
            } else if (opportuniteId != null) {
                taches = tacheService.getTachesByOpportunite(opportuniteId);
            } else {
                taches = tacheService.getAllTaches(); 
            }
            
            return ResponseEntity.ok(taches);
            
        } catch (EntityNotFound e) {
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage());
        }
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