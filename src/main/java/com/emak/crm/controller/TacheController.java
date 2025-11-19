package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emak.crm.service.TacheService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/taches")
public class TacheController {
    
 

    @GetMapping
    public String taches(
        @RequestParam(name = "clientId", required = false) Long clientId,
        @RequestParam(name = "opportuniteId", required = false) Long opportuniteId,
        @RequestParam(name = "utilisateurId", required = false) Long utilisateurId,
        Model model) {
        
        // Ajouter les données nécessaires au modèle
        model.addAttribute("clientId", clientId);
        model.addAttribute("opportuniteId", opportuniteId);
        model.addAttribute("utilisateurId", utilisateurId);
        
        return "pages/taches/taches";
    }
}