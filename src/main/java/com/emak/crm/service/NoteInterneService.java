package com.emak.crm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.NoteInterneRequest;
import com.emak.crm.dto.NoteInterneResponse;
import com.emak.crm.entity.Utilisateur;
@Service
public interface NoteInterneService {

	//List<NoteInterneResponse> findAllByUtilisateur(Utilisateur utilisateur);

	List<NoteInterneResponse> findByClientId(Long clientId, Utilisateur utilisateur);

	NoteInterneResponse createNote(NoteInterneRequest request, Utilisateur auteur);

	NoteInterneResponse updateNote(Long idNote, NoteInterneRequest request, Utilisateur utilisateur);

	void deleteNote(Long idNote, Utilisateur utilisateur);

	//List<NoteInterneResponse> searchNotes(String searchTerm, Utilisateur utilisateur);
}
