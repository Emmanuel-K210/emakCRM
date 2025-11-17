package com.emak.crm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.OpportuniteRequest;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.exception.EntityNotFound;
@Service
public interface OpportuniteService extends CrudService<OpportuniteRequest, OpportuniteResponse>{
	 	OpportuniteResponse createOpportunite(OpportuniteRequest request) throws EntityNotFound;
	    OpportuniteResponse getOpportunite(Long id) throws EntityNotFound;
	    List<OpportuniteResponse> getAllOpportunites();
	    Page<OpportuniteResponse> getAllOpportunites(Pageable pageable);
	    List<OpportuniteResponse> getOpportunitesByEtape(String etape);
	    List<OpportuniteResponse> getOpportunitesByCommercial(Long commercialId) throws EntityNotFound;
	    OpportuniteResponse updateOpportunite(Long id, OpportuniteRequest request) throws EntityNotFound;
	    void deleteOpportunite(Long id) throws EntityNotFound;
	    void changerEtapeOpportunite(Long id, String nouvelleEtape) throws EntityNotFound;
	    BigDecimal calculerCABrut();
	    Map<String, Object> getStatsPipeline();
	    
	    // Méthodes de compatibilité
	    List<OpportuniteResponse> findAll();
	    OpportuniteResponse save(OpportuniteRequest request);
	    OpportuniteResponse saver(OpportuniteRequest request) throws EntityNotFound;
	    OpportuniteResponse findById(Number id) throws EntityNotFound;
	    void deleteById(Number id) throws EntityNotFound;
	    OpportuniteResponse update(Number id, OpportuniteRequest request) throws EntityNotFound;
	    void changerEtapeOpportunite(Number id, String nouvelleEtape) throws EntityNotFound;
	    Page<OpportuniteResponse> findAll(Pageable pageable);
	    List<OpportuniteResponse> getOpportuniteByClientId(Long clientId);
}
