package com.emak.crm.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.NoteInterneMapper;
import com.emak.crm.dto.NoteInterneRequest;
import com.emak.crm.dto.NoteInterneResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.NoteInterne;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.NoteInterneRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.service.NoteInterneService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@AllArgsConstructor
public class NoteInterneServiceImpl implements NoteInterneService{
    
    private final NoteInterneRepository noteInterneRepository;
    private final ClientRepository clientRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final PermissionService permissionService;
    
   /* @Override
    public List<NoteInterneResponse> findAllByUtilisateur(Utilisateur utilisateur) {
        List<NoteInterne> notes = noteInterneRepository.findByPubliqueOrAuteur(utilisateur);
        return notes.stream()
                .map(NoteInterneMapper::toResponse)
                .collect(Collectors.toList());
    }*/
    
    @Override
    public List<NoteInterneResponse> findByClientId(Long clientId, Utilisateur utilisateur) {
        // Vérifier que l'utilisateur peut voir ce client
        if (!permissionService.peutVoirClient(utilisateur, clientId)) {
            throw new SecurityException("Accès non autorisé à ce client");
        }
        
        List<NoteInterne> notes = noteInterneRepository.findByClientId(clientId);
        return notes.stream()
                .filter(note -> !note.getPrivee() || note.getAuteur().equals(utilisateur))
                .map(NoteInterneMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @Override
    public NoteInterneResponse createNote(NoteInterneRequest request, Utilisateur auteur) {
        NoteInterne note = NoteInterneMapper.toEntity(request, auteur);
        
        // Lier le client si spécifié
        if (request.idClient() != null) {
            Client client = clientRepository.findById(request.idClient())
                    .orElseThrow(() -> new IllegalArgumentException("Client non trouvé"));
            note.setClient(client);
        }
        
        // Lier l'opportunité si spécifiée
        if (request.idOpportunite() != null) {
            Opportunite opportunite = opportuniteRepository.findById(request.idOpportunite())
                    .orElseThrow(() -> new IllegalArgumentException("Opportunité non trouvée"));
            note.setOpportunite(opportunite);
        }
        
        NoteInterne savedNote = noteInterneRepository.save(note);
        return NoteInterneMapper.toResponse(savedNote);
    }
    
    @Override
    @Transactional
    public NoteInterneResponse updateNote(Long idNote, NoteInterneRequest request, Utilisateur utilisateur) {
        NoteInterne note = noteInterneRepository.findById(idNote)
                .orElseThrow(() -> new IllegalArgumentException("Note non trouvée"));
        
        // Vérifier que l'utilisateur peut modifier cette note
        if (!note.getAuteur().equals(utilisateur)) {
            throw new SecurityException("Seul l'auteur peut modifier cette note");
        }
        
        NoteInterneMapper.updateEntityFromRequest(note, request);
        NoteInterne updatedNote = noteInterneRepository.save(note);
        return NoteInterneMapper.toResponse(updatedNote);
    }
    
    @Override
    @Transactional
    public void deleteNote(Long idNote, Utilisateur utilisateur) {
        NoteInterne note = noteInterneRepository.findById(idNote)
                .orElseThrow(() -> new IllegalArgumentException("Note non trouvée"));
        
        // Vérifier que l'utilisateur peut supprimer cette note
        if (!note.getAuteur().equals(utilisateur) && 
            !permissionService.estAdmin(utilisateur)) {
            throw new SecurityException("Non autorisé à supprimer cette note");
        }
        
        noteInterneRepository.delete(note);
    }
    
  /*  @Override
    public List<NoteInterneResponse> searchNotes(String searchTerm, Utilisateur utilisateur) {
        // ✅ CORRIGÉ : Utiliser la bonne méthode
        List<NoteInterne> notes = noteInterneRepository.searchByTitreOrContenuAndAuteur(searchTerm, utilisateur);
        return notes.stream()
                .map(NoteInterneMapper::toResponse)
                .collect(Collectors.toList());
    }*/
}