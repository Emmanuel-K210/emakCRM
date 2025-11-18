package com.emak.crm.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.TypeClient;
import com.emak.crm.exception.EntityNotFound;
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
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        model.addAttribute("clients", clientService.findAll(PageRequest.of(page, size)));
        model.addAttribute("pageActive", "clients");
        return "pages/clients/clients";
    }
    
    @GetMapping("/ajouter")
    public String ajouterClient(Model model) {
        model.addAttribute("pageActive", "clients");
        model.addAttribute("typeClients", TypeClient.values());
        model.addAttribute("origineClients", OrigineClient.values());
        model.addAttribute("client", new ClientRequest("", "", "", "", "", "", "", "", "", "", "", null, null, null, null, null, null));
        return "pages/clients/nouveau";
    }
    
    @PostMapping("/ajouter")
    public String posterClient(@Valid @ModelAttribute("client") ClientRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
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
            return "pages/clients/felication";    		
    	}catch(EntityNotFound e) {
    		return "redirect:/clients/ajouter";
    	}

    }
}