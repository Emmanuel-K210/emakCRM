package com.emak.crm.service.impl;

import org.springframework.stereotype.Service;

import com.emak.crm.entity.Client;
import com.emak.crm.entity.NoteInterne;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.Roles;
import com.emak.crm.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ClientRepository clientRepository;

    // Vérifications générales par rôle
    public boolean estAdmin(Utilisateur utilisateur) {
        return utilisateur.getRole() == Roles.ADMIN;
    }

    public boolean estManagerCommercial(Utilisateur utilisateur) {
        return utilisateur.getRole() == Roles.MANAGER_COMMERCIAL;
    }

    public boolean estCommercial(Utilisateur utilisateur) {
        return utilisateur.getRole() == Roles.COMMERCIAL;
    }

    public boolean peutVoirTousLesClients(Utilisateur utilisateur) {
        return estAdmin(utilisateur) || estManagerCommercial(utilisateur);
    }

    public boolean peutVoirDonneesFinancieres(Utilisateur utilisateur) {
        return estAdmin(utilisateur) || estManagerCommercial(utilisateur) || estCommercial(utilisateur);
    }

    // Permissions spécifiques par entité
    public boolean peutVoirClient(Utilisateur utilisateur, Long clientId) {
        if (peutVoirTousLesClients(utilisateur)) {
            return true;
        }
        
        // Un commercial ne voit que ses clients
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));
        
        return client.getUtilisateurResponsable() != null && 
               client.getUtilisateurResponsable().equals(utilisateur);
    }

    public boolean peutModifierClient(Utilisateur utilisateur, Client client) {
        if (estAdmin(utilisateur)) return true;
        if (estManagerCommercial(utilisateur)) return true;
        
        // Un commercial ne peut modifier que ses clients
        return estCommercial(utilisateur) && 
               client.getUtilisateurResponsable() != null && 
               client.getUtilisateurResponsable().equals(utilisateur);
    }

    public boolean peutSupprimerClient(Utilisateur utilisateur, Client client) {
        // Seul l'admin peut supprimer définitivement
        return estAdmin(utilisateur);
    }

    public boolean peutVoirOpportunite(Utilisateur utilisateur, Opportunite opportunite) {
        if (peutVoirTousLesClients(utilisateur)) return true;
        
        // Vérifier si l'opportunité appartient à l'utilisateur
        return opportunite.getUtilisateur().equals(utilisateur) ||
               (opportunite.getClient().getUtilisateurResponsable() != null && 
                opportunite.getClient().getUtilisateurResponsable().equals(utilisateur));
    }

    public boolean peutModifierOpportunite(Utilisateur utilisateur, Opportunite opportunite) {
        if (estAdmin(utilisateur)) return true;
        if (estManagerCommercial(utilisateur)) return true;
        
        return opportunite.getUtilisateur().equals(utilisateur);
    }

    public boolean peutVoirNote(Utilisateur utilisateur, NoteInterne note) {
        // Notes publiques ou notes de l'utilisateur
        return !note.getPrivee() || note.getAuteur().equals(utilisateur);
    }

    public boolean peutModifierNote(Utilisateur utilisateur, NoteInterne note) {
        // Seul l'auteur peut modifier sa note
        return note.getAuteur().equals(utilisateur);
    }
}