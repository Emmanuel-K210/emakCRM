package com.emak.crm.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.dto.PaiementResponse;
import com.emak.crm.entity.Facture;
import com.emak.crm.entity.Paiement;
import com.emak.crm.enums.ModePaiement;
import com.emak.crm.enums.StatutPaiement;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.mapper.PaiementMapper;
import com.emak.crm.repository.FactureRepository;
import com.emak.crm.repository.PaiementRepository;
import com.emak.crm.service.PaiementService;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final FactureRepository factureRepository;


    private Paiement getById(Long id) throws EntityNotFound {
        return paiementRepository.findById(id)
                .orElseThrow(() -> EntityNotFound.of("Le paiement"));
    }
    
    private Facture getFacture(Long id) throws EntityNotFound {
    	return factureRepository.findById(id).orElseThrow(()->EntityNotFound.of("La facture n'a pas été trouvé"));
    }
    @Override
    public PaiementResponse save(PaiementRequest request) throws EntityNotFound {
    	 Facture facture = factureRepository.findById(request.idFacture())
                 .orElseThrow(() -> EntityNotFound.of("Facture non trouvée avec l'id: " + request.idFacture()));

         // Conversion en entité
         Paiement paiement = PaiementMapper.toEntity(request, facture);
         
         // Sauvegarde
         Paiement paiementSauvegarde = paiementRepository.save(paiement);
         
         log.info("Paiement de {} € enregistré pour la facture {}", 
                 request.montant(), facture.getNumeroFacture());
         
        return PaiementMapper.toResponse(paiementRepository.save(paiement));
    }

    @Override
    public Page<PaiementResponse> findAll(Pageable pageable) {
        return paiementRepository.findAll(pageable).map(PaiementMapper::toResponse);
    }

    @Override
    public PaiementResponse findById(Long id) throws EntityNotFound {
        return PaiementMapper.toResponse(getById(id));
    }

    @Override
    public PaiementResponse update(Long id, PaiementRequest request) throws EntityNotFound {
        Paiement paiement = getById(id);
        paiement.setMontant(request.montant());
        paiement.setModePaiement(ModePaiement.valueOf(request.modePaiement()));
        paiement.setStatut(StatutPaiement.valueOf(request.statut()));
        if (!paiement.getFacture().getId().equals(request.idFacture())) {
            paiement.setFacture(getFacture(request.idFacture()));
        }
        return PaiementMapper.toResponse(paiementRepository.save(paiement));
    }

    @Override
    public void deleteById(Long id) throws EntityNotFound {
        Paiement paiement = getById(id);
        paiementRepository.delete(paiement);
    }
}
