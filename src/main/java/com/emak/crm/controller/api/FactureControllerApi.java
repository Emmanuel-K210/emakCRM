package com.emak.crm.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.FactureRequest;
import com.emak.crm.dto.FactureResponse;
import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.FactureService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/factures")
@AllArgsConstructor
public class FactureControllerApi {

    private final FactureService factureService;

    @GetMapping
    public ResponseEntity<Page<FactureResponse>> getAllFactures(
            Pageable pageable,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String dateTo) {
        
        // Pour l'instant retourne toutes les factures, on peut ajouter des filtres plus tard
        return ResponseEntity.ok(factureService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponse> getFacture(@PathVariable Long id) throws EntityNotFound {
        return ResponseEntity.ok(factureService.getFacture(id));
    }

    @PostMapping
    public ResponseEntity<FactureResponse> createFacture(@Valid @RequestBody FactureRequest request) {
        FactureResponse response = factureService.createFacture(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/paiements")
    public ResponseEntity<FactureResponse> enregistrerPaiement(
            @PathVariable Long id, 
            @Valid @RequestBody PaiementRequest request) throws EntityNotFound {
        
        return ResponseEntity.ok(factureService.enregistrerPaiement(id, request));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FactureResponse>> getFacturesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(factureService.getFacturesByClient(clientId));
    }

    @GetMapping("/en-retard")
    public ResponseEntity<List<FactureResponse>> getFacturesEnRetard() {
        return ResponseEntity.ok(factureService.getFacturesEnRetard());
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<FactureResponse> annulerFacture(
            @PathVariable Long id, 
            @RequestParam String raison) throws EntityNotFound {
        
        return ResponseEntity.ok(factureService.annulerFacture(id, raison));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        // Implémentation à prévoir si nécessaire
        return ResponseEntity.noContent().build();
    }
}