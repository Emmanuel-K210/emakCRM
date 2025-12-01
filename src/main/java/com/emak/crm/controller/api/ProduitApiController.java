package com.emak.crm.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.emak.crm.dto.ProduitRequest;
import com.emak.crm.dto.ProduitResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.ProduitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "http://localhost:8081")
@RequiredArgsConstructor
@Slf4j
public class ProduitApiController {

    private final ProduitService produitService;

    @GetMapping
    public ResponseEntity<Page<ProduitResponse>> getAllProduits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProduitResponse> produits = produitService.findAll(pageable);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProduitResponse>> getAllProduitsList() {
        List<ProduitResponse> produits = produitService.findAll();
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduitById(@PathVariable Long id) {
        try {
            ProduitResponse produit = produitService.findById(id);
            return ResponseEntity.ok(produit);
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduit(@Valid @RequestBody ProduitRequest request) {
        try {
            ProduitResponse nouveauProduit = produitService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouveauProduit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du produit");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduit(@PathVariable Long id, @Valid @RequestBody ProduitRequest request) {
        try {
            log.info("🚀 PUT /api/produits/{} appelé", id);
            log.info("📦 Données reçues: {}", request);
            
            ProduitResponse produitMaj = produitService.update(id, request);
            
            log.info("✅ Produit mis à jour avec succès: {}", produitMaj.nomProduit());
            return ResponseEntity.ok(produitMaj);
            
        } catch (EntityNotFound e) {
            log.error("❌ Produit non trouvé: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ Erreur validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("💥 Erreur serveur: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du produit");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduit(@PathVariable Long id) {
        try {
            produitService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<?> getProduitsByCategorie(@PathVariable String categorie) {
        try {
            List<ProduitResponse> produits = produitService.getProduitsByCategorie(categorie);
            return ResponseEntity.ok(produits);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ProduitResponse>> rechercherProduits(@RequestParam String q) {
        List<ProduitResponse> produits = produitService.rechercherProduits(q);
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/rupture-stock")
    public ResponseEntity<List<ProduitResponse>> getProduitsEnRupture() {
        List<ProduitResponse> produits = produitService.getProduitsEnRupture();
        return ResponseEntity.ok(produits);
    }

    @GetMapping("/stock-faible")
    public ResponseEntity<List<ProduitResponse>> getProduitsStockFaible() {
        List<ProduitResponse> produits = produitService.getProduitsStockFaible();
        return ResponseEntity.ok(produits);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        try {
            produitService.updateStock(id, stock);
            return ResponseEntity.ok().build();
        } catch (EntityNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}