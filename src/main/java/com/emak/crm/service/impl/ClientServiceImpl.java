package com.emak.crm.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.dto.ClientResponse;
import com.emak.crm.dto.ClientSearchRequest;
import com.emak.crm.dto.ClientSearchResponse;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.ClientMapper;
import com.emak.crm.mapper.OpportuniteMapper;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.service.ClientService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {

	private ClientRepository clientRepository;

	/**
	 * CONSULTATION FICHE CLIENT Métier : Accéder à toutes les informations d'un
	 * client Données : Coordonnées, historique, opportunités, interactions
	 */
	private Client getById(Number id) throws EntityNotFound {
		return clientRepository.findById((long) id).orElseThrow(() -> EntityNotFound.of("Le client"));
	}

	/**
	 * LISTE CLIENTS AVEC FILTRES Métier : Vue globale du portefeuille clients Usage
	 * : Suivi commercial, reporting, actions de masse
	 */
	@Override
	public List<ClientResponse> findAll() {
		return clientRepository.findAll().stream().map(ClientMapper::toResponse).toList();
	}

	/**
	 * LISTE CLIENTS AVEC FILTRES Métier : Vue globale du portefeuille clients Usage
	 * : Suivi commercial, reporting, actions de masse
	 */
	@Override
	public Page<ClientResponse> findAll(Pageable pageable) {
		Page<Client> page = clientRepository.findAll(pageable);
		return page.map(ClientMapper::toResponse);
	}

	/**
	 * CRÉATION D'UN CLIENT/PROSPECT Métier : Enregistrer une nouvelle entreprise ou
	 * contact commercial Workflow : Validation → Assignation commercial → Calcul
	 * score → Sauvegarde Règles : Email unique par entreprise, commercial
	 * obligatoire
	 */
	@Override
	public ClientResponse save(ClientRequest requete) {

		return ClientMapper.toResponse(clientRepository.save(ClientMapper.toEntity(requete)));
	}

	/**
	 * CONSULTATION FICHE CLIENT Métier : Accéder à toutes les informations d'un
	 * client Données : Coordonnées, historique, opportunités, interactions
	 */
	@Override
	public ClientResponse findById(Number id) throws EntityNotFound {
		return ClientMapper.toResponse(getById(id));
	}

	/**
	 * SUPPRESSION CLIENT Métier : Archiver un client (ne supprime pas vraiment)
	 * Règles : Vérifier qu'aucune opportunité active n'existe
	 */
	@Override
	public void deleteById(Number id) throws EntityNotFound {
		Client client = getById(id);
		clientRepository.delete(client);
	}

	/**
	 * MISE À JOUR CLIENT Métier : Modifier les informations d'un client existant
	 * Workflow : Vérification permissions → Validation → Historique modifications
	 */
	@Override
	public ClientResponse update(Number id, ClientRequest requete) throws EntityNotFound {
		Client client = getById(id);
		client.setAdresse(requete.adresse());
		client.setEmail(requete.email());
		client.setEntreprise(requete.entreprise());
		client.setNom(requete.nom());
		client.setTelephone(requete.telephone());
		client.setAdresse(requete.adresse());
		client.setTypeClient(TypeClient.valueOf(requete.typeClient()));
		return ClientMapper.toResponse(clientRepository.save(client));
	}

	/**
	 * SUGGESTIONS POUR AUTO-COMPLÉTION Métier : Fournir des suggestions en temps
	 * réel pendant la saisie
	 */
	public List<String> getSuggestions(String term) {
		if (term == null || term.length() < 2) {
			return Collections.emptyList();
		}
		return clientRepository.findSuggestions("%" + term.toLowerCase() + "%");
	}

	/**
	 * LISTE DES VILLES DISTINCTES Métier : Obtenir toutes les villes pour les
	 * filtres
	 */
	@Override
	public List<String> getVillesDistinctes() {
		return clientRepository.findAll().stream().map(Client::getVille)
				.filter(ville -> ville != null && !ville.trim().isEmpty()).distinct().sorted()
				.collect(Collectors.toList());
	}

	/**
	 * STATISTIQUES DE RECHERCHE Métier : Analyser les résultats de recherche
	 */
	public Map<String, Object> getSearchStats(ClientSearchRequest searchRequest) {
		ClientSearchResponse results = searchClientsWithFilters(searchRequest);

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalResults", results.clients().size());
		stats.put("prospectsCount", results.clients().stream().filter(c -> "PROSPECT".equals(c.typeClient())).count());
		stats.put("clientsCount", results.clients().stream().filter(c -> "CLIENT".equals(c.typeClient())).count());
		stats.put("averageScore", results.clients().stream().mapToInt(ClientResponse::scoreProspect).average().orElse(0.0));

		return stats;
	}

	/**
	 * CONSTRUCTION DU TRI
	 */
	private Sort buildSort(String triPar, String ordreTri) {
		Sort.Direction direction = "DESC".equalsIgnoreCase(ordreTri) ? Sort.Direction.DESC : Sort.Direction.ASC;

		switch (triPar != null ? triPar.toLowerCase() : "nom") {
		case "entreprise":
			return Sort.by(direction, "entreprise");
		case "ville":
			return Sort.by(direction, "ville");
		case "score":
			return Sort.by(direction, "scoreProspect");
		case "date":
			return Sort.by(direction, "dateCreation");
		case "commercial":
			return Sort.by(direction, "utilisateurResponsable.nom");
		default:
			return Sort.by(direction, "nom");
		}
	}

	/**
	 * RECHERCHE CLIENTS AVEC PAGINATION Métier : Recherche avancée avec pagination
	 * pour performances
	 */
	public ClientSearchResponse searchClientsWithFilters(ClientSearchRequest searchRequest) {
		Specification<Client> spec = buildSearchSpecification(searchRequest);

		// Configuration du tri
		Sort sort = buildSort(searchRequest.triPar(), searchRequest.ordreTri());

		// Pagination
		Pageable pageable = PageRequest.of(searchRequest.page() - 1, searchRequest.size(), sort);

		Page<Client> clientPage = clientRepository.findAll(spec, pageable);

		// Calcul des statistiques
		Map<String, Object> stats = calculateSearchStats(spec);

		return new ClientSearchResponse(
				clientPage.getContent().stream().map(ClientMapper::toResponse).collect(Collectors.toList()),
				searchRequest.page(), clientPage.getTotalPages(), clientPage.getTotalElements(), searchRequest.size(),
				searchRequest.searchTerm(), stats, getSuggestionsForSearch(searchRequest.searchTerm()));
	}

	/**
     * CONSTRUCTION DE LA SPÉCIFICATION DE RECHERCHE
     */
    private Specification<Client> buildSearchSpecification(ClientSearchRequest searchRequest) {
        return Specification.where(ClientSpecifications.withSearchTerm(searchRequest.searchTerm()))
                .and(ClientSpecifications.withTypeClient(searchRequest.typeClient().name()))
                .and(ClientSpecifications.withStatut(searchRequest.statut().name()))
                .and(ClientSpecifications.withVille(searchRequest.ville()))
                .and(ClientSpecifications.withScoreMin(searchRequest.scoreMin()))
                .and(ClientSpecifications.withScoreMax(searchRequest.scoreMax()))
                .and(ClientSpecifications.withCommercial(searchRequest.idUtilisateurResponsable()))
                .and(ClientSpecifications.withDateCreationBetween(
                        searchRequest.dateCreationDebut(), searchRequest.dateCreationFin()));
    }

	/**
	 * CALCUL DES STATISTIQUES DE RECHERCHE
	 */
	private Map<String, Object> calculateSearchStats(Specification<Client> spec) {
		List<Client> allResults = clientRepository.findAll(spec);

		Map<TypeClient, Long> countByType = allResults.stream()
				.collect(Collectors.groupingBy(Client::getTypeClient, Collectors.counting()));

		Map<StatutClient, Long> countByStatut = allResults.stream()
				.collect(Collectors.groupingBy(Client::getStatut, Collectors.counting()));

		Map<String, Long> countByVille = allResults.stream().filter(c -> c.getVille() != null)
				.collect(Collectors.groupingBy(Client::getVille, Collectors.counting()));

		double avgScore = allResults.stream().mapToInt(Client::getScoreProspect).average().orElse(0.0);

		return Map.of("totalResults", (long) allResults.size(), "countByType", countByType, "countByStatut",
				countByStatut, "countByVille", countByVille, "averageScore", avgScore, "prospectsCount",
				countByType.getOrDefault(TypeClient.PROSPECT, 0L), "clientsCount",
				countByType.getOrDefault(TypeClient.CLIENT, 0L));
	}

	/**
	 * SUGGESTIONS CONTEXTUELLES
	 */
	private List<String> getSuggestionsForSearch(String searchTerm) {
		if (searchTerm == null || searchTerm.length() < 2) {
			return List.of();
		}

		return clientRepository.findSuggestions("%" + searchTerm.toLowerCase() + "%")
				.stream()
				.limit(10) // Limiter à suggestions
				.collect(Collectors.toList());
	}

	/**
	 * EXPORT DES RÉSULTATS DE RECHERCHE
	 * 
	 * public byte[] exportSearchResults(ExportRequest exportRequest) {
	 * ClientSearchResponse searchResults = searchClientsWithFilters(
	 * exportRequest.searchCriteria() );
	 * 
	 * switch (exportRequest.format().toUpperCase()) { case "EXCEL": return
	 * exportToExcel(searchResults.clients(), exportRequest.colonnes()); case "PDF":
	 * return exportToPdf(searchResults.clients(), exportRequest.colonnes()); case
	 * "CSV": return exportToCsv(searchResults.clients(), exportRequest.colonnes());
	 * default: throw new IllegalArgumentException("Format non supporté: " +
	 * exportRequest.format()); } }
	 */

	@Override
	public List<ClientResponse> searchClients(String searchTerm) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * MISE À JOUR SCORE PROSPECT
	 * Métier : Ajuster le score de potentiel d'un prospect
	 * Usage : Priorisation des actions commerciales
	 * Échelle : 1-10 (1=froid, 10=chaud)
	 * @throws EntityNotFound 
	 */
	@Override
	public void updateScoreClient(Long clientId, Integer nouveauScore) throws EntityNotFound {
		Client client = getById(clientId);
		client.setScoreProspect(nouveauScore);
		clientRepository.save(client);
	}
	
	/**
	 * LISTE DES PROSPECTS
	 * Métier : Clients non encore convertis en clients actifs
	 * Usage : Actions de prospection, campagnes marketing
	 */
	@Override
	public List<ClientResponse> getProspects() {
		
		return clientRepository.findAll()
				.stream()
				.filter(client->client.getTypeClient()==TypeClient.PROSPECT)
				.map(ClientMapper::toResponse)
				.toList();
	}
	
	/**
	 * LISTE CLIENTS ACTIFS
	 * Métier : Clients ayant déjà acheté ou en cours de projet
	 * Usage : Suivi relation, ventes additionnelles
	 */
	@Override
	public List<ClientResponse> getClientsActifs() {
		
		return clientRepository.findAll()
				.stream()
				.filter(client->client.getTypeClient()!=TypeClient.PROSPECT)
				.map(ClientMapper::toResponse)
				.toList();
	}
	
	@Override
	public Page<Client> findClientsWithFilters(String search, String statut, String type, String sort, Pageable pageable) {
	        Specification<Client> spec = Specification.where(null);
	        
	        if (search != null && !search.trim().isEmpty()) {
	            spec = spec.and((root, query, cb) -> 
	                cb.or(
	                    cb.like(cb.lower(root.get("nom")), "%" + search.toLowerCase() + "%"),
	                    cb.like(cb.lower(root.get("prenom")), "%" + search.toLowerCase() + "%"),
	                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
	                    cb.like(cb.lower(root.get("entreprise")), "%" + search.toLowerCase() + "%")
	                )
	            );
	        }
	        
	        if (statut != null && !statut.isEmpty()) {
	            spec = spec.and((root, query, cb) -> 
	                cb.equal(root.get("statut"), StatutClient.valueOf(statut))
	            );
	        }
	        
	        if (type != null && !type.isEmpty()) {
	            spec = spec.and((root, query, cb) -> 
	                cb.equal(root.get("typeClient"), TypeClient.valueOf(type))
	            );
	        }
	        
	        // Tri
	        if ("nom".equals(sort)) {
	            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
	                Sort.by("nom", "prenom"));
	        } else if ("entreprise".equals(sort)) {
	            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
	                Sort.by("entreprise"));
	        } else if ("score".equals(sort)) {
	            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
	                Sort.by("scoreProspect").descending());
	        } else {
	            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
	                Sort.by("dateCreation").descending());
	        }
	        
	        return clientRepository.findAll(spec, pageable);
	    }
	    
	    public Map<String, Long> getClientStats() {
	        Map<String, Long> stats = new HashMap<>();
	        stats.put("totalClients", clientRepository.count());
	        stats.put("clientsActifs", clientRepository.countByStatut(StatutClient.ACTIF));
	        stats.put("prospects", clientRepository.countByStatut(StatutClient.INACTIF));
	        stats.put("nouveauxClients", clientRepository.countByDateCreationAfter(LocalDateTime.now().minusDays(7)));
	        return stats;
	    }

		@Override
		public List<OpportuniteResponse> findOpportuniteesByClientId(Long id) throws EntityNotFound {
			var client = getById(id);
			return client.getOpportunitees().stream()
					.map(OpportuniteMapper::toResponse)
					.toList();
		}	

}
