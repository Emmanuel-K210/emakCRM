package com.emak.crm.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emak.crm.exception.EntityNotFound;

public interface CrudService <R,U>{
	
	List<U> findAll();
	U save(R requete);
	U findById(Number id) throws EntityNotFound;
	void deleteById(Number id) throws EntityNotFound;
	U update(Number id,R requete) throws EntityNotFound;
	Page<U> findAll(Pageable spageable);
}
