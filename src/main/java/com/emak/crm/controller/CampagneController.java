package com.emak.crm.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emak.crm.dto.CampagneRequest;
import com.emak.crm.dto.CampagneResponse;
import com.emak.crm.enums.StatutCampagne;
import com.emak.crm.enums.TypeCampagne;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.service.CampagneService;
import com.emak.crm.service.UtilisateurService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/campagnes")
@AllArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public String liste(Model model,
                         @PageableDefault(size = 10, sort = "dateCreation") Pageable pageable) {
        Page<CampagneResponse> page = campagneService.findAll(pageable);
        model.addAttribute("campagnes", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageActive", "campagnes");
        return "pages/campagnes/campagnes";
    }

    @GetMapping("/ajouter")
    public String formAjouter(Model model) {
        model.addAttribute("campagne", CampagneRequest.builder().build());
        model.addAttribute("types", TypeCampagne.values());
        model.addAttribute("statuts", StatutCampagne.values());
        model.addAttribute("utilisateurs", utilisateurService.findAll());
        model.addAttribute("modeEdition", false);
        model.addAttribute("pageActive", "campagnes");
        return "pages/campagnes/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(@Valid @ModelAttribute("campagne") CampagneRequest campagne,
                           BindingResult result, Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("types", TypeCampagne.values());
            model.addAttribute("statuts", StatutCampagne.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            model.addAttribute("modeEdition", false);
            return "pages/campagnes/form";
        }
        campagneService.save(campagne);
        redirectAttributes.addFlashAttribute("success", "✅ Campagne créée avec succès !");
        return "redirect:/campagnes";
    }

    @GetMapping("/modifier/{id}")
    public String formModifier(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CampagneResponse campagne = campagneService.findById(id);
            CampagneRequest request = CampagneRequest.builder()
                    .nomCampagne(campagne.getNomCampagne())
                    .type(campagne.getType())
                    .dateDebut(campagne.getDateDebut())
                    .dateFin(campagne.getDateFin())
                    .budget(campagne.getBudget())
                    .objectif(campagne.getObjectif())
                    .statut(campagne.getStatut())
                    .tauxConversion(campagne.getTauxConversion())
                    .utilisateurResponsableId(campagne.getUtilisateurResponsable() != null
                            ? campagne.getUtilisateurResponsable().getId() : null)
                    .build();
            model.addAttribute("campagne", request);
            model.addAttribute("campagneId", id);
            model.addAttribute("types", TypeCampagne.values());
            model.addAttribute("statuts", StatutCampagne.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            model.addAttribute("modeEdition", true);
            model.addAttribute("pageActive", "campagnes");
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/campagnes";
        }
        return "pages/campagnes/form";
    }

    @PostMapping("/modifier/{id}")
    public String modifier(@PathVariable Long id,
                            @Valid @ModelAttribute("campagne") CampagneRequest campagne,
                            BindingResult result, Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("campagneId", id);
            model.addAttribute("types", TypeCampagne.values());
            model.addAttribute("statuts", StatutCampagne.values());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            model.addAttribute("modeEdition", true);
            return "pages/campagnes/form";
        }
        try {
            campagneService.update(id, campagne);
            redirectAttributes.addFlashAttribute("success", "✏️ Campagne modifiée avec succès !");
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/campagnes";
    }

    @GetMapping("/supprimer/{id}")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            campagneService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "🗑️ Campagne supprimée avec succès !");
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/campagnes";
    }

    @GetMapping("/executer/{id}")
    public String executer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            campagneService.executerCampagne(id);
            redirectAttributes.addFlashAttribute("success", "🚀 Campagne exécutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/campagnes";
    }
}
