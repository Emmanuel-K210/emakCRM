package com.emak.crm.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.DevisRequest;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.DevisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/devis")
@CrossOrigin(origins = "http://localhost:8081")
@RequiredArgsConstructor
public class DevisControllerApi {
	private final DevisService devisService;
	
	@GetMapping("/{id}")
    public String getDevisDetail(@PathVariable Long id, Model model) {
        try {
            DevisResponse devis = devisService.findById(id);
            model.addAttribute("devis", devis);
            return "devis-detail";
        } catch (EntityNotFound e) {
            return "redirect:/devis";
        }
    }

    // === API REST ===

    @GetMapping("/api")
    public ResponseEntity<Page<DevisResponse>> getAllDevis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DevisResponse> devis = devisService.findAll(pageable);
        return ResponseEntity.ok(devis);
    }

    @GetMapping("/api/{id}")
    public ResponseEntity<?> getDevisById(@PathVariable Long id) {
        try {
            DevisResponse devis = devisService.findById(id);
            return ResponseEntity.ok(devis);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api")
    public ResponseEntity<?> createDevis(@Valid @RequestBody DevisRequest request) {
        try {
            DevisResponse nouveauDevis = devisService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouveauDevis);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du devis");
        }
    }

    @PutMapping("/api/{id}")
    public ResponseEntity<?> updateDevis(@PathVariable Long id, @Valid @RequestBody DevisRequest request) {
        try {
            DevisResponse devisMaj = devisService.update(id, request);
            return ResponseEntity.ok(devisMaj);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    public ResponseEntity<?> deleteDevis(@PathVariable Long id) {
        try {
            devisService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/envoyer")
    public ResponseEntity<?> envoyerDevis(@PathVariable Long id) {
        try {
            DevisResponse devis = devisService.envoyerDevis(id);
            return ResponseEntity.ok(devis);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/{id}/convertir-facture")
    public ResponseEntity<?> convertirEnFacture(@PathVariable Long id) {
        try {
            DevisResponse devis = devisService.convertirEnFacture(id);
            return ResponseEntity.ok(devis);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/statut/{statut}")
    public ResponseEntity<List<DevisResponse>> getDevisByStatut(@PathVariable String statut) {
        // Implémentation selon votre besoin
        return ResponseEntity.ok(List.of());
    }
}
