package com.emak.crm.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.dto.ClientResponse;
import com.emak.crm.dto.ClientSearchRequest;
import com.emak.crm.dto.ClientSearchResponse;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.exception.EntityNotFound;

@Service
public interface ClientService extends CrudService<ClientRequest,ClientResponse>{
	List<ClientResponse> searchClients(String searchTerm);
	void updateScoreClient(Long clientId, Integer nouveauScore) throws EntityNotFound;
	List<ClientResponse> getProspects();
	List<ClientResponse> getClientsActifs();
	/**
	 * LISTE DES VILLES DISTINCTES Métier : Obtenir toutes les villes pour les
	 * filtres
	 */
	 List<String> getVillesDistinctes();
	 Map<String, Object> getSearchStats(ClientSearchRequest searchRequest);
	 ClientSearchResponse searchClientsWithFilters(ClientSearchRequest searchRequest);
	 Map<String, Long> getClientStats();
	 Page<Client> findClientsWithFilters(String search, String statut, String type, String sort, Pageable pageable);
	 List<OpportuniteResponse> findOpportuniteesByClientId(Long id)throws EntityNotFound;
}
