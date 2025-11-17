package com.emak.crm.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emak.crm.dto.ProduitRequest;
import com.emak.crm.dto.ProduitResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.ProduitService;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping(path = "/produits")
public class ProduitController {

	private final ProduitService produitService; 
	
	
	@GetMapping
    public String produits(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        Page<ProduitResponse> produitsPage = produitService.findAll(PageRequest.of(page, size));
        model.addAttribute("produitsPage", produitsPage);
        model.addAttribute("pageActive", "produits");
        return "pages/produits/produits.html";
    }

    // Enregistrer ou mettre à jour un produit
    @PostMapping("/enregistrer")
    public String enregistrer(Long id,ProduitRequest produitRequest, RedirectAttributes attributes) {
    	if(id==null) {    		
    		produitService.save(produitRequest);
    		attributes.addFlashAttribute("successMessage", "Produit enregistré avec succès !");
    	}else {
    		try {
				produitService.update(id, produitRequest);
				attributes.addFlashAttribute("successMessage", "Produit mis à jour avec succès !");
			} catch (EntityNotFound e) {
				attributes.addFlashAttribute("erreurMessage", "Produit n'a été pas de mis a jour !");
			}
    	}
        return "redirect:/produits";
    }

    // Supprimer un produit
    @GetMapping("/supprimer")
    public String supprimer(@RequestParam Long id, RedirectAttributes attributes) {
        try {
            produitService.deleteById(id);
            attributes.addFlashAttribute("successMessage", "Produit supprimé avec succès !");
        } catch (EntityNotFound e) {
            attributes.addFlashAttribute("suppressionError", e.getMessage());
        }
        return "redirect:/produits";
    }

    // Endpoint pour récupérer un produit pour l'édition AJAX
    @GetMapping("/get/{id}")
    @ResponseBody
    public ProduitResponse obtenir(@PathVariable Long id,RedirectAttributes attributes) {
        try {
			return produitService.findById(id);
		} catch (EntityNotFound e) {
			attributes.addAttribute("produitInconnu", e.getMessage());
			return null;
		}
    }

}
