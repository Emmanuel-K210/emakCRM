package com.emak.crm.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.dto.ClientResponse;
import com.emak.crm.dto.ClientSearchRequest;
import com.emak.crm.dto.ClientSearchResponse;
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
}
