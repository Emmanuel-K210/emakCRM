package com.emak.crm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.PaiementRequest;
import com.emak.crm.dto.PaiementResponse;
import com.emak.crm.exception.EntityNotFound;
@Service
public interface PaiementService {

    PaiementResponse save(PaiementRequest request) throws EntityNotFound;

    Page<PaiementResponse> findAll(Pageable pageable);

    PaiementResponse findById(Long id) throws EntityNotFound;

    PaiementResponse update(Long id, PaiementRequest request) throws EntityNotFound;

    void deleteById(Long id) throws EntityNotFound;
}
