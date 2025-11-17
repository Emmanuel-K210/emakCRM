package com.emak.crm.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emak.crm.dto.DevisRequest;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.ClientService;
import com.emak.crm.service.DevisService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/devis")
public class DevisController {

    private final DevisService devisService;
    private final ClientService clientService; 

    @GetMapping
    public String devis(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        Page<DevisResponse> devisPage = devisService.findAll(PageRequest.of(page, size));
        model.addAttribute("devisPage", devisPage);
        model.addAttribute("pageActive", "devis");
        model.addAttribute("clients", clientService.findAll());
        return "pages/devis/devis";
    }

    @PostMapping("/enregistrer")
    public String enregistrer(DevisRequest requete) throws EntityNotFound {
        devisService.save(requete);
        return "redirect:/devis";
    }

    @GetMapping("/supprimer")
    public String supprimer(@RequestParam Long id, RedirectAttributes attributes) {
        try {
            devisService.deleteById(id);
        } catch (EntityNotFound e) {
            attributes.addFlashAttribute("suppressionError", e.getMessage());
        }
        return "redirect:/devis";
    }

    @PostMapping("/modifier")
    public String modifier(@RequestParam Long id, DevisRequest requete, RedirectAttributes attributes) {
        try {
            devisService.update(id, requete);
        } catch (EntityNotFound e) {
            attributes.addFlashAttribute("updateError", e.getMessage());
        }
        return "redirect:/devis";
    }
}
