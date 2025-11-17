package com.emak.crm.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.CampagneRequest;
import com.emak.crm.dto.CampagneResponse;
import com.emak.crm.entity.Campagne;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.Envoi;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutCampagne;
import com.emak.crm.enums.StatutEnvoi;
import com.emak.crm.enums.TypeEnvoi;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.CampagneMapper;
import com.emak.crm.repository.CampagneRepository;
import com.emak.crm.repository.EnvoiRepository;
import com.emak.crm.repository.UtilisateurRepository;
import com.emak.crm.service.CampagneService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class CampagneServiceImpl implements CampagneService{

	private final CampagneRepository campagneRepository;
	private final UtilisateurRepository utilisateurRepository;
	private final EmailService emailService;
	private final SMSService smsService;
	private final EnvoiRepository envoiRepository;
	
	/**
	 * CAMPAGNES ACTIVES
	 * Métier : Obtenir toutes les Campagnes 
	 * Usage : Suivi exécution, coordination
	 */
	@Override
	public List<CampagneResponse> findAll() {
		
		return campagneRepository
				.findAll()
				.stream()
				.map(CampagneMapper::toResponseDTO)
				.toList();
	}

	/**
	 * CRÉER UNE CAMPAGNE
	 * Métier : Planifier une action marketing ciblée
	 * Types : Email, SMS, événement, réseaux sociaux
	 */
	@Override
	public CampagneResponse save(CampagneRequest requete) {
		Optional<Utilisateur> utilisateur = utilisateurRepository.findById(requete.getUtilisateurResponsableId()); 
		Campagne campagne = CampagneMapper.toEntity(requete);
		if(!utilisateur.isPresent()) {
			EntityNotFound.of("Aucun Responsable n'a été trouvé");
		}
		campagne.setUtilisateurResponsable(utilisateur.get());
		
		return CampagneMapper.toResponseDTO(campagneRepository.save(campagne));
	}

	@Override
	public CampagneResponse findById(Number id) throws EntityNotFound {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteById(Number id) throws EntityNotFound {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CampagneResponse update(Number id, CampagneRequest requete) throws EntityNotFound {
		
		return null;
	}

	@Override
	public Page<CampagneResponse> findAll(Pageable spageable) {
		// TODO Auto-generated method stub
		return null;
	}
		
	/**
	 * CAMPAGNES ACTIVES
	 * Métier : Campagnes en cours de diffusion ou planifiées
	 * Usage : Suivi exécution, coordination
	 */
	@Override
	public List<CampagneResponse> getCampagnesActives() {
		return campagneRepository
				.getCampagnesActives(Arrays.asList(StatutCampagne.EN_COURS,StatutCampagne.PLANIFIEE))
				.stream().map(CampagneMapper::toResponseDTO)
				.toList()
				;
	}
	
	
	@Transactional
    public void executerCampagne(Long campagneId) {
        // 1. RÉCUPÉRATION ET VALIDATION
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
        
        if (campagne.getStatut() != StatutCampagne.PLANIFIEE) {
            throw new RuntimeException("Seules les campagnes planifiées peuvent être exécutées");
        }
        
        // 2. PRÉPARATION DES DONNÉES
        List<Client> cibles = collecterCiblesCampagne(campagne);
        
        if (cibles.isEmpty()) {
            throw new RuntimeException("Aucune cible trouvée pour cette campagne");
        }
        
        // 3. MISE À JOUR STATUT
        campagne.setStatut(StatutCampagne.EN_COURS);
        campagneRepository.save(campagne);
        
        // 4. DIFFUSION (selon le type de campagne)
        switch (campagne.getType()) {
            case EMAILING:
                executerCampagneEmail(campagne, cibles);
                break;
            case SMS:
                executerCampagneSMS(campagne, cibles);
                break;
            default:
                throw new RuntimeException("Type de campagne non supporté");
        }
        
        // 5. LOGS ET SUIVI
        log.info("Campagne {} exécutée avec succès. {} cibles contactées.", 
                   campagne.getNomCampagne(), cibles.size());
    }
    
    private List<Client> collecterCiblesCampagne(Campagne campagne) {
        // Récupérer tous les clients des listes de diffusion associées
        return campagne.getListes().stream()
                .flatMap(liste -> liste.getClients().stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
    private void executerCampagneEmail(Campagne campagne, List<Client> cibles) {
        for (Client client : cibles) {
        	Envoi envoi = new Envoi();
            try {
                // Créer l'envoi en base
                 envoi = Envoi.builder()
                        .type(TypeEnvoi.EMAIL)
                        .objet(campagne.getNomCampagne())
                        .contenu(genererContenuEmail(campagne, client))
                        .statut(StatutEnvoi.EN_COURS)
                        .dateEnvoi(LocalDateTime.now())
                        .campagne(campagne)
                        .client(client)
                        .build();
                
                envoiRepository.save(envoi);
                
                // Envoyer l'email
                emailService.envoyerEmail(
                    client.getEmail(),
                    envoi.getObjet(),
                    envoi.getContenu()
                );
                
                // Mettre à jour le statut
                envoi.setStatut(StatutEnvoi.ENVOYE);
                envoiRepository.save(envoi);
                
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi à {}", client.getEmail(), e);
                // Marquer l'envoi en échec
                envoi.setStatut(StatutEnvoi.ECHOUE);
                envoiRepository.save(envoi);
            }
        }
    }
    
    private void executerCampagneSMS(Campagne campagne, List<Client> cibles) {
        for (Client client : cibles) {
        	Envoi envoi = new Envoi();
            try {
                 envoi = Envoi.builder()
                        .type(TypeEnvoi.SMS)
                        .contenu(genererContenuSMS(campagne, client))
                        .statut(StatutEnvoi.ENVOYE)
                        .dateEnvoi(LocalDateTime.now())
                        .campagne(campagne)
                        .client(client)
                        .build();
                
                envoiRepository.save(envoi);
                
                smsService.envoyerSMS(client.getTelephone(), envoi.getContenu());
                
            } catch (Exception e) {
                log.error("Erreur SMS à {}", client.getTelephone(), e);
                envoi.setStatut(StatutEnvoi.ECHOUE);
                envoiRepository.save(envoi);
            }
        }
    }
    
    private String genererContenuEmail(Campagne campagne, Client client) {
        // Personnalisation du contenu
        return String.format("""
            Bonjour %s %s,
            
            %s
            
            Cordialement,
            L'équipe marketing
            """, 
            client.getPrenom(), 
            client.getNom(),
            campagne.getObjectif()
        );
    }
    
    private String genererContenuSMS(Campagne campagne, Client client) {
        return String.format("Bonjour %s, %s", 
            client.getPrenom(), 
            campagne.getObjectif()
        );
    }

}
