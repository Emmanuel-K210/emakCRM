package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/factures")
@AllArgsConstructor
public class FactureController {

    @GetMapping
    public String listFactures() {
        return "pages/factures/factures";
    }

    @GetMapping("/{id}")
    public String viewFacture(@PathVariable Long id, Model model) {
        model.addAttribute("factureId", id);
        return "pages/factures/facture";
    }

    @GetMapping("/nouveau")
    public String newFacture() {
        return "pages/factures/form";
    }

    @GetMapping("/{id}/editer")
    public String editFacture(@PathVariable Long id, Model model) {
        model.addAttribute("factureId", id);
        return "pages/factures/form";
    }
}