package com.emak.crm.controller;

import com.emak.crm.dto.OpportuniteRequest;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.OpportuniteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/opportunites")
@AllArgsConstructor
public class OpportuniteController {

    private final OpportuniteService opportuniteService;

    @GetMapping
    public String listeOpportunites(Model model,
                                    @PageableDefault(size = 10, sort = "dateCreation") Pageable pageable) {

        Page<OpportuniteResponse> page = opportuniteService.findAll(pageable);
        model.addAttribute("opportunites", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageActive", "opportunites");

        return "pages/opportunite/liste.html";
    }

    @GetMapping("/ajouter")
    public String formAjouter(Model model) {
        model.addAttribute("opportunite", new Opportunite());
        model.addAttribute("pageActive", "opportunites");
        return "pages/opportunite/form.html";
    }

    @PostMapping("/ajouter")
    public String ajouter(@ModelAttribute OpportuniteRequest opportunite, RedirectAttributes redirectAttributes) {
        opportuniteService.save(opportunite);
        redirectAttributes.addFlashAttribute("success", "✅ Opportunité ajoutée avec succès !");
        return "redirect:/opportunites";
    }

    @GetMapping("/modifier/{id}")
    public String formModifier(@PathVariable Long id, Model model,RedirectAttributes attributes) {
        OpportuniteResponse opportunite;
		try {
			opportunite = opportuniteService.findById(id);
			model.addAttribute("opportunite", opportunite);
		    model.addAttribute("pageActive", "opportunites");
		} catch (EntityNotFound e) {
			attributes.addFlashAttribute("erreur",e.getMessage());
			return null;
		}
      
        return "pages/opportunite/form.html";
    }

    @PostMapping("/modifier/{id}")
    public String modifier(@PathVariable Number id,
                           @ModelAttribute OpportuniteRequest opportunite,
                           RedirectAttributes redirectAttributes) {
        try {
			opportuniteService.update(id, opportunite);
			redirectAttributes.addFlashAttribute("success", "✏️ Opportunité modifiée avec succès !");
		} catch (EntityNotFound e) {
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
			return null;
		}
        return "redirect:/opportunites";
    }

    @GetMapping("/supprimer/{id}")
    public String supprimer(@PathVariable Number id, RedirectAttributes redirectAttributes) {
        try {
			opportuniteService.deleteById(id);
			redirectAttributes.addFlashAttribute("success", "🗑️ Opportunité supprimée avec succès !");
		} catch (EntityNotFound e) {
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
			return null;
		}
        return "redirect:/opportunites";
    }
}
