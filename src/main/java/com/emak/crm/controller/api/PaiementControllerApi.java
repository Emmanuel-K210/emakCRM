package com.emak.crm.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.dto.PaiementResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.PaiementService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/paiements")
@AllArgsConstructor
public class PaiementControllerApi {

    private final PaiementService paiementService;

    @GetMapping
    public ResponseEntity<Page<PaiementResponse>> getAllPaiements(Pageable pageable) {
        return ResponseEntity.ok(paiementService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementResponse> getPaiement(@PathVariable Long id) throws EntityNotFound {
        return ResponseEntity.ok(paiementService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaiementResponse> createPaiement(@Valid @RequestBody PaiementRequest request) throws EntityNotFound {
        return ResponseEntity.ok(paiementService.save(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaiementResponse> updatePaiement(
            @PathVariable Long id, 
            @Valid @RequestBody PaiementRequest request) throws EntityNotFound {
        
        return ResponseEntity.ok(paiementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Long id) throws EntityNotFound {
        paiementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}