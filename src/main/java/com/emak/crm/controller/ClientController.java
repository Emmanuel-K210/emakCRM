package com.emak.crm.controller;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.dto.ClientResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.TypeClient;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.ClientMapper;
import com.emak.crm.service.ClientService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/clients")
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    public String clients(Model model,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "statut", required = false) String statut,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "sort", defaultValue = "dateCreation") String sort) {
        
        try {
            // Utilisation de votre méthode existante avec les filtres
            Page<Client> clientsPage = clientService.findClientsWithFilters(search, statut, type, sort, PageRequest.of(page, size));
            
            // Conversion en ClientResponse 
            Page<ClientResponse> clientsResponsePage = clientsPage.map(ClientMapper::toResponse);
            
            // Récupération des statistiques
            Map<String, Long> stats = clientService.getClientStats();
                        
            model.addAttribute("clients", clientsResponsePage);
            model.addAttribute("stats", stats);
            model.addAttribute("typeClients", java.util.Arrays.stream(TypeClient.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toList()));
            model.addAttribute("pageActive", "clients");
            
        } catch (Exception e) {
            // Gestion d'erreur - page vide avec message
            model.addAttribute("clients", Page.empty());
            model.addAttribute("stats", Map.of(
                "totalClients", 0L,
                "clientsActifs", 0L, 
                "prospects", 0L,
                "nouveauxClients", 0L
            ));
            model.addAttribute("error", "Erreur lors du chargement des clients");
        }
        
        return "pages/clients/clients";
    }
    
    @GetMapping("/ajouter")
    public String ajouterClient(Model model) {
        model.addAttribute("pageActive", "clients");
        model.addAttribute("typeClients", TypeClient.values());
        return "pages/clients/nouveau";
    }
    
    @PostMapping("/ajouter")
    public String posterClient(ClientRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
        	System.out.println("error name: "+result.getObjectName());
            return "pages/clients/nouveau";
        }
        
        try {
            var client = clientService.save(request);
            redirectAttributes.addFlashAttribute("success", "Client créé avec succès !");
            return "redirect:/clients/felicitation?id=" + client.id();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/clients/ajouter";
        }
    }
    
    @GetMapping("/felicitation")
    public String felicitation(@RequestParam Long id, Model model) {
        try {
            var client = clientService.findById(id);
            model.addAttribute("client", client);
            model.addAttribute("pageActive", "clients");
            return "pages/clients/felicitation";
        } catch (EntityNotFound e) {
            return "redirect:/clients";
        }
    }

    // Méthode pour voir la fiche détaillée d'un client
    @GetMapping("/{id}")
    public String voirClient(@PathVariable Long id, Model model) {
        try { 
            model.addAttribute("client", clientService.findById(id));
            model.addAttribute("opportunitees",clientService.findOpportuniteesByClientId(id));
            model.addAttribute("pageActive", "clients");
            return "pages/clients/fiche";
        } catch (EntityNotFound e) {
            return "redirect:/clients";
        }
    }

    // Méthode pour modifier un client
    @GetMapping("/{id}/modifier")
    public String modifierClient(@PathVariable Long id, Model model) {
        try {
            var client = clientService.findById(id);
            model.addAttribute("client", client);
            model.addAttribute("typeClient", TypeClient.values());
            model.addAttribute("origineClient", OrigineClient.values());
            model.addAttribute("pageActive", "clients");
            return "pages/clients/modifier";
        } catch (EntityNotFound e) {
            return "redirect:/clients";
        }
    }

    @PostMapping("/{id}/modifier")
    public String posterModification(@PathVariable Long id,
                                   @Valid @ModelAttribute("client") ClientRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "pages/clients/modifier";
        }
        
        try {
            var client = clientService.update(id, request);
            redirectAttributes.addFlashAttribute("success", "Client modifié avec succès !");
            return "redirect:/clients/" + id;
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/clients/" + id + "/modifier";
        }
    }

    // Méthode pour supprimer un client
    @PostMapping("/{id}/supprimer")
    public String supprimerClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Client supprimé avec succès !");
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("error", "Client non trouvé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/clients";
    }

    // Méthode pour mettre à jour le score d'un client
    @PostMapping("/{id}/score")
    public String updateScore(@PathVariable Long id,
                            @RequestParam Integer score,
                            RedirectAttributes redirectAttributes) {
        try {
            clientService.updateScoreClient(id, score);
            redirectAttributes.addFlashAttribute("success", "Score mis à jour avec succès !");
        } catch (EntityNotFound e) {
            redirectAttributes.addFlashAttribute("error", "Client non trouvé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du score : " + e.getMessage());
        }
        return "redirect:/clients/" + id;
    }
}