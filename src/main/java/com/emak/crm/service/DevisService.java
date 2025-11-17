package com.emak.crm.service;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.DevisRequest;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.exception.EntityNotFound;
@Service
public interface DevisService extends CrudService<DevisRequest, DevisResponse>{
	DevisResponse findByIdPourConversion(Number id) throws EntityNotFound;
	 DevisResponse envoyerDevis(Long id) throws EntityNotFound;
	 DevisResponse convertirEnFacture(Long id) throws EntityNotFound;
	 
}
