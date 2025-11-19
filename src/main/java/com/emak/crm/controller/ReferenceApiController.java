package com.emak.crm.controller;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.ClientRefDTO;
import com.emak.crm.dto.OpportuniteRefDTO;
import com.emak.crm.dto.UtilisateurRefDTO;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.repository.UtilisateurRepository;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/references")
@AllArgsConstructor
public class ReferenceApiController {
    
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final OpportuniteRepository opportuniteRepository;

    @GetMapping("/utilisateurs")
    public List<UtilisateurRefDTO> getUtilisateurs() {
        return utilisateurRepository.findAll().stream()
            .map(u -> new UtilisateurRefDTO(
                u.getId(),
                u.getNom() + " " + u.getPrenom(),
                u.getEmail()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/clients")
    public List<ClientRefDTO> getClients() {
        return clientRepository.findAll().stream()
            .map(c -> new ClientRefDTO(
                c.getId(),
                c.getNom(),
                c.getEntreprise()
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/opportunites")
    public List<OpportuniteRefDTO> getOpportunites() {
        return opportuniteRepository.findAll().stream()
            .map(o -> new OpportuniteRefDTO(
                o.getId(),
                o.getNomOpportunite(),
                o.getClient().getNom()
            ))
            .collect(Collectors.toList());
    }
}